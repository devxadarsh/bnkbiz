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
package com.stellar.bnkbiz.portfolio.savings.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.stellar.bnkbiz.infrastructure.core.domain.LocalDateInterval;
import com.stellar.bnkbiz.organisation.monetary.domain.MonetaryCurrency;
import com.stellar.bnkbiz.organisation.monetary.domain.Money;
import com.stellar.bnkbiz.portfolio.account.service.AccountTransfersReadPlatformService;
import com.stellar.bnkbiz.portfolio.savings.SavingsPostingInterestPeriodType;
import com.stellar.bnkbiz.portfolio.savings.domain.interest.CompoundInterestHelper;
import com.stellar.bnkbiz.portfolio.savings.domain.interest.PostingPeriod;
import java.time.LocalDate;

public final class SavingsHelper {

    AccountTransfersReadPlatformService accountTransfersReadPlatformService = null;

    public SavingsHelper(AccountTransfersReadPlatformService accountTransfersReadPlatformService) {
        this.accountTransfersReadPlatformService = accountTransfersReadPlatformService;
    }

    private final CompoundInterestHelper compoundInterestHelper = new CompoundInterestHelper();

    public List<LocalDateInterval> determineInterestPostingPeriods(final LocalDate startInterestCalculationLocalDate,
            final LocalDate interestPostingUpToDate, final SavingsPostingInterestPeriodType postingPeriodType,
            final Integer financialYearBeginningMonth) {

        final List<LocalDateInterval> postingPeriods = new ArrayList<>();

        LocalDate periodStartDate = startInterestCalculationLocalDate;
        LocalDate periodEndDate = periodStartDate;

        while (!periodStartDate.isAfter(interestPostingUpToDate) && !periodEndDate.isAfter(interestPostingUpToDate)) {

            final LocalDate interestPostingLocalDate = determineInterestPostingPeriodEndDateFrom(periodStartDate, postingPeriodType,
                    interestPostingUpToDate, financialYearBeginningMonth);
            periodEndDate = interestPostingLocalDate.minusDays(1);

            postingPeriods.add(LocalDateInterval.create(periodStartDate, periodEndDate));

            periodEndDate = interestPostingLocalDate;
            periodStartDate = interestPostingLocalDate;
        }

        return postingPeriods;
    }

    private LocalDate determineInterestPostingPeriodEndDateFrom(final LocalDate periodStartDate,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final LocalDate interestPostingUpToDate,
            Integer financialYearBeginningMonth) {

        LocalDate periodEndDate = interestPostingUpToDate;
        final Integer monthOfYear = periodStartDate.getMonthOfYear();
        financialYearBeginningMonth--;
        if (financialYearBeginningMonth == 0) financialYearBeginningMonth = 12;

        final ArrayList<LocalDate> quarterlyDates = new ArrayList<>();
        quarterlyDates.add(periodStartDate.withMonthOfYear(financialYearBeginningMonth).dayOfMonth().withMaximumValue());
        quarterlyDates.add(periodStartDate.withMonthOfYear(financialYearBeginningMonth).plusMonths(3).withYear(periodStartDate.getYear())
                .dayOfMonth().withMaximumValue());
        quarterlyDates.add(periodStartDate.withMonthOfYear(financialYearBeginningMonth).plusMonths(6).withYear(periodStartDate.getYear())
                .dayOfMonth().withMaximumValue());
        quarterlyDates.add(periodStartDate.withMonthOfYear(financialYearBeginningMonth).plusMonths(9).withYear(periodStartDate.getYear())
                .dayOfMonth().withMaximumValue());
        Collections.sort(quarterlyDates);

        final ArrayList<LocalDate> biannualDates = new ArrayList<>();
        biannualDates.add(periodStartDate.withMonthOfYear(financialYearBeginningMonth).dayOfMonth().withMaximumValue());
        biannualDates.add(periodStartDate.withMonthOfYear(financialYearBeginningMonth).plusMonths(6).withYear(periodStartDate.getYear())
                .dayOfMonth().withMaximumValue());
        Collections.sort(biannualDates);

        boolean isEndDateSet = false;

        switch (interestPostingPeriodType) {
            case INVALID:
            break;
            case MONTHLY:
                // produce period end date on last day of current month
                periodEndDate = periodStartDate.dayOfMonth().withMaximumValue();
            break;
            case QUATERLY:
                for (LocalDate quarterlyDate : quarterlyDates) {
                    if (quarterlyDate.isAfter(periodStartDate)) {
                        periodEndDate = quarterlyDate;
                        isEndDateSet = true;
                        break;
                    }
                }

                if (!isEndDateSet) periodEndDate = quarterlyDates.get(0).plusYears(1).dayOfMonth().withMaximumValue();
            break;
            case BIANNUAL:
                for (LocalDate biannualDate : biannualDates) {
                    if (biannualDate.isAfter(periodStartDate)) {
                        periodEndDate = biannualDate;
                        isEndDateSet = true;
                        break;
                    }
                }

                if (!isEndDateSet) periodEndDate = biannualDates.get(0).plusYears(1).dayOfMonth().withMaximumValue();
            break;
            case ANNUAL:
                if (financialYearBeginningMonth < monthOfYear) {
                    periodEndDate = periodStartDate.withMonthOfYear(financialYearBeginningMonth);
                    periodEndDate = periodEndDate.plusYears(1);
                } else {
                    periodEndDate = periodStartDate.withMonthOfYear(financialYearBeginningMonth);
                }
                periodEndDate = periodEndDate.dayOfMonth().withMaximumValue();
            break;
        }

        // interest posting always occurs on next day after the period end date.
        periodEndDate = periodEndDate.plusDays(1);

        return periodEndDate;
    }

    public Money calculateInterestForAllPostingPeriods(final MonetaryCurrency currency, final List<PostingPeriod> allPeriods,
            LocalDate accountLockedUntil, Boolean immediateWithdrawalOfInterest) {
        return this.compoundInterestHelper.calculateInterestForAllPostingPeriods(currency, allPeriods, accountLockedUntil,
                immediateWithdrawalOfInterest);
    }

    public Collection<Long> fetchPostInterestTransactionIds(Long accountId) {
        return this.accountTransfersReadPlatformService.fetchPostInterestTransactionIds(accountId);
    }
}