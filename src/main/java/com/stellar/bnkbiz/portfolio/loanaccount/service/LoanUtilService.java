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
package com.stellar.bnkbiz.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.List;

import com.stellar.bnkbiz.infrastructure.configuration.domain.ConfigurationDomainService;
import com.stellar.bnkbiz.organisation.holiday.domain.Holiday;
import com.stellar.bnkbiz.organisation.holiday.domain.HolidayRepository;
import com.stellar.bnkbiz.organisation.holiday.domain.HolidayStatusType;
import com.stellar.bnkbiz.organisation.monetary.domain.ApplicationCurrency;
import com.stellar.bnkbiz.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import com.stellar.bnkbiz.organisation.monetary.domain.MonetaryCurrency;
import com.stellar.bnkbiz.organisation.workingdays.domain.WorkingDays;
import com.stellar.bnkbiz.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import com.stellar.bnkbiz.portfolio.calendar.domain.Calendar;
import com.stellar.bnkbiz.portfolio.calendar.domain.CalendarEntityType;
import com.stellar.bnkbiz.portfolio.calendar.domain.CalendarInstance;
import com.stellar.bnkbiz.portfolio.calendar.domain.CalendarInstanceRepository;
import com.stellar.bnkbiz.portfolio.calendar.service.CalendarUtils;
import com.stellar.bnkbiz.portfolio.floatingrates.data.FloatingRateDTO;
import com.stellar.bnkbiz.portfolio.floatingrates.data.FloatingRatePeriodData;
import com.stellar.bnkbiz.portfolio.floatingrates.exception.FloatingRateNotFoundException;
import com.stellar.bnkbiz.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import com.stellar.bnkbiz.portfolio.loanaccount.data.HolidayDetailDTO;
import com.stellar.bnkbiz.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.Loan;
import com.stellar.bnkbiz.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoanUtilService {

    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final HolidayRepository holidayRepository;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final LoanScheduleGeneratorFactory loanScheduleFactory;
    private final FloatingRatesReadPlatformService floatingRatesReadPlatformService;

    @Autowired
    public LoanUtilService(final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            final CalendarInstanceRepository calendarInstanceRepository, final ConfigurationDomainService configurationDomainService,
            final HolidayRepository holidayRepository, final WorkingDaysRepositoryWrapper workingDaysRepository,
            final LoanScheduleGeneratorFactory loanScheduleFactory, final FloatingRatesReadPlatformService floatingRatesReadPlatformService) {
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.calendarInstanceRepository = calendarInstanceRepository;
        this.configurationDomainService = configurationDomainService;
        this.holidayRepository = holidayRepository;
        this.workingDaysRepository = workingDaysRepository;
        this.loanScheduleFactory = loanScheduleFactory;
        this.floatingRatesReadPlatformService = floatingRatesReadPlatformService;
    }

    public ScheduleGeneratorDTO buildScheduleGeneratorDTO(final Loan loan, final LocalDate recalculateFrom) {
        final HolidayDetailDTO holidayDetailDTO = null;
        return buildScheduleGeneratorDTO(loan, recalculateFrom, holidayDetailDTO);
    }

    public ScheduleGeneratorDTO buildScheduleGeneratorDTO(final Loan loan, final LocalDate recalculateFrom,
            final HolidayDetailDTO holidayDetailDTO) {
        HolidayDetailDTO holidayDetails = holidayDetailDTO;
        if (holidayDetailDTO == null) {
            holidayDetails = constructHolidayDTO(loan);
        }
        final MonetaryCurrency currency = loan.getCurrency();
        ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                CalendarEntityType.LOANS.getValue());
        LocalDate calculatedRepaymentsStartingFromDate = this.getCalculatedRepaymentsStartingFromDate(loan.getDisbursementDate(), loan,
                calendarInstance);
        CalendarInstance restCalendarInstance = null;
        CalendarInstance compoundingCalendarInstance = null;
        Long overdurPenaltyWaitPeriod = null;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(loan.loanInterestRecalculationDetailId(),
                    CalendarEntityType.LOAN_RECALCULATION_REST_DETAIL.getValue());
            compoundingCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(
                    loan.loanInterestRecalculationDetailId(), CalendarEntityType.LOAN_RECALCULATION_COMPOUNDING_DETAIL.getValue());
            overdurPenaltyWaitPeriod = this.configurationDomainService.retrievePenaltyWaitPeriod();
        }
        FloatingRateDTO floatingRateDTO = constructFloatingRateDTO(loan);
        ScheduleGeneratorDTO scheduleGeneratorDTO = new ScheduleGeneratorDTO(loanScheduleFactory, applicationCurrency,
                calculatedRepaymentsStartingFromDate, holidayDetails, restCalendarInstance, compoundingCalendarInstance, recalculateFrom,
                overdurPenaltyWaitPeriod, floatingRateDTO);

        return scheduleGeneratorDTO;
    }

    public LocalDate getCalculatedRepaymentsStartingFromDate(final Loan loan) {
        final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                CalendarEntityType.LOANS.getValue());
        return this.getCalculatedRepaymentsStartingFromDate(loan.getDisbursementDate(), loan, calendarInstance);
    }

    private HolidayDetailDTO constructHolidayDTO(final Loan loan) {
        final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(), loan
                .getDisbursementDate().toDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();

        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays, allowTransactionsOnHoliday,
                allowTransactionsOnNonWorkingDay);
        return holidayDetailDTO;
    }

    private FloatingRateDTO constructFloatingRateDTO(final Loan loan) {
        FloatingRateDTO floatingRateDTO = null;
        if (loan.loanProduct().isLinkedToFloatingInterestRate()) {
            boolean isFloatingInterestRate = loan.getIsFloatingInterestRate();
            BigDecimal interestRateDiff = loan.getInterestRateDifferential();
            List<FloatingRatePeriodData> baseLendingRatePeriods = null;
            try {
                baseLendingRatePeriods = this.floatingRatesReadPlatformService.retrieveBaseLendingRate().getRatePeriods();
            } catch (final FloatingRateNotFoundException ex) {
                // Do not do anything
            }

            floatingRateDTO = new FloatingRateDTO(isFloatingInterestRate, loan.getDisbursementDate(), interestRateDiff,
                    baseLendingRatePeriods);
        }
        return floatingRateDTO;
    }

    private LocalDate getCalculatedRepaymentsStartingFromDate(final LocalDate actualDisbursementDate, final Loan loan,
            final CalendarInstance calendarInstance) {
        final Calendar calendar = calendarInstance == null ? null : calendarInstance.getCalendar();
        return calculateRepaymentStartingFromDate(actualDisbursementDate, loan, calendar);
    }

    public LocalDate getCalculatedRepaymentsStartingFromDate(final LocalDate actualDisbursementDate, final Loan loan,
            final Calendar calendar) {
        if (calendar == null) { return getCalculatedRepaymentsStartingFromDate(loan); }
        return calculateRepaymentStartingFromDate(actualDisbursementDate, loan, calendar);

    }

    private LocalDate calculateRepaymentStartingFromDate(final LocalDate actualDisbursementDate, final Loan loan, final Calendar calendar) {
        LocalDate calculatedRepaymentsStartingFromDate = loan.getExpectedFirstRepaymentOnDate();
        if (calendar != null) {// sync repayments

            // TODO: AA - user provided first repayment date takes precedence
            // over recalculated meeting date
            if (calculatedRepaymentsStartingFromDate == null) {
                // FIXME: AA - Possibility of having next meeting date
                // immediately after disbursement date,
                // need to have minimum number of days gap between disbursement
                // and first repayment date.
                final LoanProductRelatedDetail repaymentScheduleDetails = loan.repaymentScheduleDetail();
                if (repaymentScheduleDetails != null) {// Not expecting to be
                                                       // null
                    final Integer repayEvery = repaymentScheduleDetails.getRepayEvery();
                    final String frequency = CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(repaymentScheduleDetails
                            .getRepaymentPeriodFrequencyType());
                    calculatedRepaymentsStartingFromDate = CalendarUtils.getFirstRepaymentMeetingDate(calendar, actualDisbursementDate,
                            repayEvery, frequency);
                }
            }
        }
        return calculatedRepaymentsStartingFromDate;
    }

}
