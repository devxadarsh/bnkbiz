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

import java.math.MathContext;
import java.util.List;
import java.util.Set;

import com.stellar.bnkbiz.organisation.monetary.domain.ApplicationCurrency;
import com.stellar.bnkbiz.organisation.monetary.domain.MonetaryCurrency;
import com.stellar.bnkbiz.portfolio.calendar.domain.Calendar;
import com.stellar.bnkbiz.portfolio.calendar.domain.CalendarInstance;
import com.stellar.bnkbiz.portfolio.floatingrates.data.FloatingRateDTO;
import com.stellar.bnkbiz.portfolio.loanaccount.data.HolidayDetailDTO;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.LoanCharge;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.LoanTransaction;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import com.stellar.bnkbiz.portfolio.loanaccount.loanschedule.data.LoanScheduleDTO;
import com.stellar.bnkbiz.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleModel;
import com.stellar.bnkbiz.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import java.time.LocalDate;

public interface LoanScheduleGenerator {

    LoanScheduleModel generate(MathContext mc, LoanApplicationTerms loanApplicationTerms, Set<LoanCharge> loanCharges,
            final HolidayDetailDTO holidayDetailDTO);

    LoanScheduleDTO rescheduleNextInstallments(MathContext mc, LoanApplicationTerms loanApplicationTerms, Set<LoanCharge> loanCharges,
            final HolidayDetailDTO holidayDetailDTO, List<LoanTransaction> transactions,
            LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor,
            List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, LocalDate rescheduleFrom);

    LoanRepaymentScheduleInstallment calculatePrepaymentAmount(MonetaryCurrency currency, LocalDate onDate,
            LoanApplicationTerms loanApplicationTerms, MathContext mc, Set<LoanCharge> charges, HolidayDetailDTO holidayDetailDTO,
            List<LoanTransaction> loanTransactions, LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor,
            List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments);

    LoanRescheduleModel reschedule(final MathContext mathContext, final LoanRescheduleRequest loanRescheduleRequest,
            final ApplicationCurrency applicationCurrency, final HolidayDetailDTO holidayDetailDTO, CalendarInstance restCalendarInstance,
            CalendarInstance compoundingCalendarInstance, final Calendar loanCalendar, FloatingRateDTO floatingRateDTO);
}