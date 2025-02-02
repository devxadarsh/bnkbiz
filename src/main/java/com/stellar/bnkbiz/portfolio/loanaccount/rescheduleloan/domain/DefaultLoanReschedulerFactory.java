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
package com.stellar.bnkbiz.portfolio.loanaccount.rescheduleloan.domain;

import java.math.MathContext;

import com.stellar.bnkbiz.organisation.monetary.domain.ApplicationCurrency;
import com.stellar.bnkbiz.portfolio.calendar.domain.Calendar;
import com.stellar.bnkbiz.portfolio.calendar.domain.CalendarInstance;
import com.stellar.bnkbiz.portfolio.floatingrates.data.FloatingRateDTO;
import com.stellar.bnkbiz.portfolio.loanaccount.data.HolidayDetailDTO;
import com.stellar.bnkbiz.portfolio.loanaccount.loanschedule.domain.DecliningBalanceInterestLoanScheduleGenerator;
import com.stellar.bnkbiz.portfolio.loanaccount.loanschedule.domain.FlatInterestLoanScheduleGenerator;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.InterestMethod;

public class DefaultLoanReschedulerFactory implements LoanReschedulerFactory {

    @Override
    public LoanRescheduleModel reschedule(final MathContext mathContext, final InterestMethod interestMethod,
            final LoanRescheduleRequest loanRescheduleRequest, final ApplicationCurrency applicationCurrency,
            final HolidayDetailDTO holidayDetailDTO, final CalendarInstance restCalendarInstance,
            final CalendarInstance compoundingCalendarInstance, final Calendar loanCalendar, final FloatingRateDTO floatingRateDTO) {

        LoanRescheduleModel loanRescheduleModel = null;

        switch (interestMethod) {
            case DECLINING_BALANCE:
                loanRescheduleModel = new DecliningBalanceInterestLoanScheduleGenerator().reschedule(mathContext, loanRescheduleRequest,
                        applicationCurrency, holidayDetailDTO, restCalendarInstance, compoundingCalendarInstance, loanCalendar,
                        floatingRateDTO);
            break;

            case FLAT:
                loanRescheduleModel = new FlatInterestLoanScheduleGenerator().reschedule(mathContext, loanRescheduleRequest,
                        applicationCurrency, holidayDetailDTO, restCalendarInstance, compoundingCalendarInstance, loanCalendar,
                        floatingRateDTO);
            break;

            case INVALID:
            break;
        }

        return loanRescheduleModel;
    }
}
