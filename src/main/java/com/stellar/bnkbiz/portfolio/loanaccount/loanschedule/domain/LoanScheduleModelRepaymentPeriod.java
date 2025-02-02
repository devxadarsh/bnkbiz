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

import java.math.BigDecimal;

import com.stellar.bnkbiz.organisation.monetary.domain.Money;
import com.stellar.bnkbiz.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import java.time.LocalDate;

/**
 * Domain representation of a Loan Schedule Repayment Period (not used for
 * persistence)
 */
public final class LoanScheduleModelRepaymentPeriod implements LoanScheduleModelPeriod {

    private final int periodNumber;
    private final LocalDate fromDate;
    private final LocalDate dueDate;
    private Money principalDue;
    private final Money outstandingLoanBalance;
    private Money interestDue;
    private Money feeChargesDue;
    private Money penaltyChargesDue;
    private Money totalDue;
    private final boolean recalculatedInterestComponent;

    public static LoanScheduleModelRepaymentPeriod repayment(final int periodNumber, final LocalDate startDate,
            final LocalDate scheduledDueDate, final Money principalDue, final Money outstandingLoanBalance, final Money interestDue,
            final Money feeChargesDue, final Money penaltyChargesDue, final Money totalDue, boolean recalculatedInterestComponent) {

        return new LoanScheduleModelRepaymentPeriod(periodNumber, startDate, scheduledDueDate, principalDue, outstandingLoanBalance,
                interestDue, feeChargesDue, penaltyChargesDue, totalDue, recalculatedInterestComponent);
    }

    public LoanScheduleModelRepaymentPeriod(final int periodNumber, final LocalDate fromDate, final LocalDate dueDate,
            final Money principalDue, final Money outstandingLoanBalance, final Money interestDue, final Money feeChargesDue,
            final Money penaltyChargesDue, final Money totalDue, final boolean recalculatedInterestComponent) {
        this.periodNumber = periodNumber;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.principalDue = principalDue;
        this.outstandingLoanBalance = outstandingLoanBalance;
        this.interestDue = interestDue;
        this.feeChargesDue = feeChargesDue;
        this.penaltyChargesDue = penaltyChargesDue;
        this.totalDue = totalDue;
        this.recalculatedInterestComponent = recalculatedInterestComponent;
    }

    @Override
    public LoanSchedulePeriodData toData() {
        return LoanSchedulePeriodData.repaymentOnlyPeriod(this.periodNumber, this.fromDate, this.dueDate, this.principalDue.getAmount(),
                this.outstandingLoanBalance.getAmount(), this.interestDue.getAmount(), this.feeChargesDue.getAmount(),
                this.penaltyChargesDue.getAmount(), this.totalDue.getAmount(), this.principalDue.plus(this.interestDue).getAmount());
    }

    @Override
    public boolean isRepaymentPeriod() {
        return true;
    }

    @Override
    public Integer periodNumber() {
        return this.periodNumber;
    }

    @Override
    public LocalDate periodFromDate() {
        return this.fromDate;
    }

    @Override
    public LocalDate periodDueDate() {
        return this.dueDate;
    }

    @Override
    public BigDecimal principalDue() {
        BigDecimal value = null;
        if (this.principalDue != null) {
            value = this.principalDue.getAmount();
        }

        return value;
    }

    @Override
    public BigDecimal interestDue() {
        BigDecimal value = null;
        if (this.interestDue != null) {
            value = this.interestDue.getAmount();
        }

        return value;
    }

    @Override
    public BigDecimal feeChargesDue() {
        BigDecimal value = null;
        if (this.feeChargesDue != null) {
            value = this.feeChargesDue.getAmount();
        }

        return value;
    }

    @Override
    public BigDecimal penaltyChargesDue() {
        BigDecimal value = null;
        if (this.penaltyChargesDue != null) {
            value = this.penaltyChargesDue.getAmount();
        }

        return value;
    }

    @Override
    public void addLoanCharges(BigDecimal feeCharge, BigDecimal penaltyCharge) {
        this.feeChargesDue = this.feeChargesDue.plus(feeCharge);
        this.penaltyChargesDue = this.penaltyChargesDue.plus(penaltyCharge);
        this.totalDue = this.totalDue.plus(feeCharge).plus(penaltyCharge);
    }

    @Override
    public void addPrincipalAmount(final Money principalDue) {
        this.principalDue = this.principalDue.plus(principalDue);
        this.totalDue = this.totalDue.plus(principalDue);
    }

    @Override
    public boolean isRecalculatedInterestComponent() {
        return this.recalculatedInterestComponent;
    }

    @Override
    public void addInterestAmount(Money interestDue) {
        this.interestDue = this.interestDue.plus(interestDue);
        this.totalDue = this.totalDue.plus(principalDue);
    }
}