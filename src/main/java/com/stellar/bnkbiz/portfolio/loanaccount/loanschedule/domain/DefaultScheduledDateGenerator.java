/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.stellar.bnkbiz.portfolio.loanaccount.loanschedule.domain;

import com.stellar.bnkbiz.organisation.holiday.service.HolidayUtil;
import com.stellar.bnkbiz.organisation.workingdays.domain.RepaymentRescheduleType;
import com.stellar.bnkbiz.organisation.workingdays.service.WorkingDaysUtil;
import com.stellar.bnkbiz.portfolio.calendar.domain.Calendar;
import com.stellar.bnkbiz.portfolio.calendar.service.CalendarUtils;
import com.stellar.bnkbiz.portfolio.common.domain.DayOfWeekType;
import com.stellar.bnkbiz.portfolio.common.domain.PeriodFrequencyType;
import com.stellar.bnkbiz.portfolio.loanaccount.data.HolidayDetailDTO;
import org.joda.time.Days;
import java.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;

public class DefaultScheduledDateGenerator implements ScheduledDateGenerator {

    @Override
    public LocalDate getLastRepaymentDate(final LoanApplicationTerms loanApplicationTerms, final HolidayDetailDTO holidayDetailDTO) {

        final int numberOfRepayments = loanApplicationTerms.getNumberOfRepayments();

        LocalDate lastRepaymentDate = loanApplicationTerms.getExpectedDisbursementDate();
        boolean isFirstRepayment = true;
        for (int repaymentPeriod = 1; repaymentPeriod <= numberOfRepayments; repaymentPeriod++) {
            lastRepaymentDate = generateNextRepaymentDate(lastRepaymentDate, loanApplicationTerms, isFirstRepayment, holidayDetailDTO);
            isFirstRepayment = false;
        }
        lastRepaymentDate = adjustRepaymentDate(lastRepaymentDate, loanApplicationTerms, holidayDetailDTO);
        return lastRepaymentDate;
    }

    @Override
    public LocalDate generateNextRepaymentDate(final LocalDate lastRepaymentDate, final LoanApplicationTerms loanApplicationTerms,
            boolean isFirstRepayment, final HolidayDetailDTO holidayDetailDTO) {
        final LocalDate firstRepaymentPeriodDate = loanApplicationTerms.getCalculatedRepaymentsStartingFromLocalDate();
        LocalDate dueRepaymentPeriodDate = null;
        if (isFirstRepayment && firstRepaymentPeriodDate != null) {
            dueRepaymentPeriodDate = firstRepaymentPeriodDate;
        } else {
            Calendar currentCalendar = loanApplicationTerms.getLoanCalendar();
            dueRepaymentPeriodDate = getRepaymentPeriodDate(loanApplicationTerms.getRepaymentPeriodFrequencyType(),
                    loanApplicationTerms.getRepaymentEvery(), lastRepaymentDate, loanApplicationTerms.getNthDay(),
                    loanApplicationTerms.getWeekDayType());
            dueRepaymentPeriodDate = CalendarUtils.adjustDate(dueRepaymentPeriodDate, loanApplicationTerms.getSeedDate(),
                    loanApplicationTerms.getRepaymentPeriodFrequencyType());
            if (currentCalendar != null) {
                // If we have currentCalendar object, this means there is a
                // calendar associated with
                // the loan, and we should use it in order to calculate next
                // repayment
                LocalDate seedDate = currentCalendar.getStartDateLocalDate();
                String reccuringString = currentCalendar.getRecurrence();
                dueRepaymentPeriodDate = CalendarUtils.getNewRepaymentMeetingDate(reccuringString, seedDate, dueRepaymentPeriodDate,
                        loanApplicationTerms.getRepaymentEvery(),
                        CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(loanApplicationTerms.getLoanTermPeriodFrequencyType()),
                        holidayDetailDTO.getWorkingDays());
            }
        }
        return dueRepaymentPeriodDate;
    }

    @Override
    public LocalDate adjustRepaymentDate(final LocalDate dueRepaymentPeriodDate, final LoanApplicationTerms loanApplicationTerms,
            final HolidayDetailDTO holidayDetailDTO) {

        LocalDate adjustedDate = dueRepaymentPeriodDate;

        LocalDate nextDueRepaymentPeriodDate = getRepaymentPeriodDate(loanApplicationTerms.getRepaymentPeriodFrequencyType(),
                loanApplicationTerms.getRepaymentEvery(), adjustedDate, loanApplicationTerms.getNthDay(),
                loanApplicationTerms.getWeekDayType());

        final RepaymentRescheduleType rescheduleType = RepaymentRescheduleType.fromInt(holidayDetailDTO.getWorkingDays()
                .getRepaymentReschedulingType());

        /**
         * Fix for https://mifosforge.jira.com/browse/MIFOSX-1357
         */
        // recursively check for the next working meeting day.
        while (WorkingDaysUtil.isNonWorkingDay(holidayDetailDTO.getWorkingDays(), nextDueRepaymentPeriodDate)
                && rescheduleType == RepaymentRescheduleType.MOVE_TO_NEXT_REPAYMENT_MEETING_DAY) {

            nextDueRepaymentPeriodDate = getRepaymentPeriodDate(loanApplicationTerms.getRepaymentPeriodFrequencyType(),
                    loanApplicationTerms.getRepaymentEvery(), nextDueRepaymentPeriodDate, loanApplicationTerms.getNthDay(),
                    loanApplicationTerms.getWeekDayType());
            nextDueRepaymentPeriodDate = CalendarUtils.adjustDate(nextDueRepaymentPeriodDate, loanApplicationTerms.getSeedDate(),
                    loanApplicationTerms.getRepaymentPeriodFrequencyType());

        }
        adjustedDate = WorkingDaysUtil.getOffSetDateIfNonWorkingDay(adjustedDate, nextDueRepaymentPeriodDate,
                holidayDetailDTO.getWorkingDays());

        if (holidayDetailDTO.isHolidayEnabled()) {
            adjustedDate = HolidayUtil.getRepaymentRescheduleDateToIfHoliday(adjustedDate, holidayDetailDTO.getHolidays());
        }

        return adjustedDate;
    }

    @Override
    public LocalDate getRepaymentPeriodDate(final PeriodFrequencyType frequency, final int repaidEvery, final LocalDate startDate,
            Integer nthDay, DayOfWeekType dayOfWeek) {
        LocalDate dueRepaymentPeriodDate = startDate;
        switch (frequency) {
            case DAYS:
                dueRepaymentPeriodDate = startDate.plusDays(repaidEvery);
            break;
            case WEEKS:
                dueRepaymentPeriodDate = startDate.plusWeeks(repaidEvery);
            break;
            case MONTHS:
                dueRepaymentPeriodDate = startDate.plusMonths(repaidEvery);
                if (!(nthDay == null || dayOfWeek == null || dayOfWeek == DayOfWeekType.INVALID)) {
                    dueRepaymentPeriodDate = adjustToNthWeekDay(dueRepaymentPeriodDate, nthDay, dayOfWeek.getValue());
                }
            break;
            case YEARS:
                dueRepaymentPeriodDate = startDate.plusYears(repaidEvery);
            break;
            case INVALID:
            break;
        }
        return dueRepaymentPeriodDate;
    }

    private LocalDate adjustToNthWeekDay(LocalDate dueRepaymentPeriodDate, int nthDay, int dayOfWeek) {
        // adjust date to start of month
        dueRepaymentPeriodDate = dueRepaymentPeriodDate.withDayOfMonth(1);
        // adjust date to next week if current day is past specified day of
        // week.
        if (dueRepaymentPeriodDate.getDayOfWeek() > dayOfWeek) {
            dueRepaymentPeriodDate = dueRepaymentPeriodDate.plusWeeks(1);
        }
        // adjust date to specified date of week
        dueRepaymentPeriodDate = dueRepaymentPeriodDate.withDayOfWeek(dayOfWeek);
        // adjust to specified nth week day
        dueRepaymentPeriodDate = dueRepaymentPeriodDate.plusWeeks(nthDay - 1);
        return dueRepaymentPeriodDate;
    }

    @Override
    public Boolean isDateFallsInSchedule(final PeriodFrequencyType frequency, final int repaidEvery, final LocalDate startDate,
            final LocalDate date) {
        boolean isScheduledDate = false;
        switch (frequency) {
            case DAYS:
                int diff = Days.daysBetween(startDate, date).getDays();
                isScheduledDate = (diff % repaidEvery) == 0;
            break;
            case WEEKS:
                int weekDiff = Weeks.weeksBetween(startDate, date).getWeeks();
                isScheduledDate = (weekDiff % repaidEvery) == 0;
                if (isScheduledDate) {
                    LocalDate modifiedDate = startDate.plusWeeks(weekDiff);
                    isScheduledDate = modifiedDate.isEqual(date);
                }
            break;
            case MONTHS:
                int monthDiff = Months.monthsBetween(startDate, date).getMonths();
                isScheduledDate = (monthDiff % repaidEvery) == 0;
                if (isScheduledDate) {
                    LocalDate modifiedDate = startDate.plusMonths(monthDiff);
                    isScheduledDate = modifiedDate.isEqual(date);
                }
            break;
            case YEARS:
                int yearDiff = Years.yearsBetween(startDate, date).getYears();
                isScheduledDate = (yearDiff % repaidEvery) == 0;
                if (isScheduledDate) {
                    LocalDate modifiedDate = startDate.plusYears(yearDiff);
                    isScheduledDate = modifiedDate.isEqual(date);
                }
            break;
            case INVALID:
            break;
        }
        return isScheduledDate;
    }

    @Override
    public LocalDate idealDisbursementDateBasedOnFirstRepaymentDate(final PeriodFrequencyType repaymentPeriodFrequencyType,
            final int repaidEvery, final LocalDate firstRepaymentDate) {

        LocalDate idealDisbursementDate = null;

        switch (repaymentPeriodFrequencyType) {
            case DAYS:
                idealDisbursementDate = firstRepaymentDate.minusDays(repaidEvery);
            break;
            case WEEKS:
                idealDisbursementDate = firstRepaymentDate.minusWeeks(repaidEvery);
            break;
            case MONTHS:
                idealDisbursementDate = firstRepaymentDate.minusMonths(repaidEvery);
            break;
            case YEARS:
                idealDisbursementDate = firstRepaymentDate.minusYears(repaidEvery);
            break;
            case INVALID:
            break;
        }

        return idealDisbursementDate;
    }

    @Override
    public LocalDate generateNextScheduleDateStartingFromDisburseDate(LocalDate lastRepaymentDate,
            LoanApplicationTerms loanApplicationTerms, final HolidayDetailDTO holidayDetailDTO) {

        LocalDate generatedDate = loanApplicationTerms.getExpectedDisbursementDate();
        boolean isFirstRepayment = true;
        while (!generatedDate.isAfter(lastRepaymentDate)) {
            generatedDate = generateNextRepaymentDate(generatedDate, loanApplicationTerms, isFirstRepayment, holidayDetailDTO);
            isFirstRepayment = false;
        }
        generatedDate = adjustRepaymentDate(generatedDate, loanApplicationTerms, holidayDetailDTO);
        return generatedDate;
    }
}