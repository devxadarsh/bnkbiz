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
import java.math.MathContext;
import java.util.Date;
import java.util.List;

import com.stellar.bnkbiz.organisation.monetary.domain.ApplicationCurrency;
import com.stellar.bnkbiz.organisation.monetary.domain.MonetaryCurrency;
import com.stellar.bnkbiz.organisation.monetary.domain.Money;
import com.stellar.bnkbiz.organisation.monetary.domain.MoneyHelper;
import com.stellar.bnkbiz.portfolio.calendar.domain.Calendar;
import com.stellar.bnkbiz.portfolio.calendar.domain.CalendarInstance;
import com.stellar.bnkbiz.portfolio.common.domain.DayOfWeekType;
import com.stellar.bnkbiz.portfolio.common.domain.DaysInMonthType;
import com.stellar.bnkbiz.portfolio.common.domain.DaysInYearType;
import com.stellar.bnkbiz.portfolio.common.domain.NthDayType;
import com.stellar.bnkbiz.portfolio.common.domain.PeriodFrequencyType;
import com.stellar.bnkbiz.portfolio.loanaccount.data.DisbursementData;
import com.stellar.bnkbiz.portfolio.loanaccount.data.LoanTermVariationsData;
import com.stellar.bnkbiz.portfolio.loanaccount.data.LoanTermVariationsDataWrapper;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.LoanInterestRecalculationDetails;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.AmortizationMethod;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.InterestMethod;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.LoanPreClosureInterestCalculationStrategy;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.joda.time.Days;
import java.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.Weeks;
import org.joda.time.Years;

public final class LoanApplicationTerms {

    private final ApplicationCurrency currency;

    private final Calendar loanCalendar;
    private Integer loanTermFrequency;
    private final PeriodFrequencyType loanTermPeriodFrequencyType;
    private Integer numberOfRepayments;
    private Integer actualNumberOfRepayments;
    private final Integer repaymentEvery;
    private final PeriodFrequencyType repaymentPeriodFrequencyType;
    private final Integer nthDay;

    private final DayOfWeekType weekDayType;
    private final AmortizationMethod amortizationMethod;

    private final InterestMethod interestMethod;
    private BigDecimal interestRatePerPeriod;
    private final PeriodFrequencyType interestRatePeriodFrequencyType;
    private BigDecimal annualNominalInterestRate;
    private final InterestCalculationPeriodMethod interestCalculationPeriodMethod;
    private final boolean allowPartialPeriodInterestCalcualtion;

    private Money principal;
    private final LocalDate expectedDisbursementDate;
    private final LocalDate repaymentsStartingFromDate;
    private final LocalDate calculatedRepaymentsStartingFromDate;
    /**
     * Integer representing the number of 'repayment frequencies' or
     * installments where 'grace' should apply to the principal component of a
     * loans repayment period (installment).
     */
    private Integer principalGrace;

    /**
     * Integer representing the number of 'repayment frequencies' or
     * installments where 'grace' should apply to the payment of interest in a
     * loans repayment period (installment).
     * 
     * <b>Note:</b> Interest is still calculated taking into account the full
     * loan term, the interest is simply offset to a later period.
     */
    private Integer interestPaymentGrace;

    /**
     * Integer representing the number of 'repayment frequencies' or
     * installments where 'grace' should apply to the charging of interest in a
     * loans repayment period (installment).
     * 
     * <b>Note:</b> The loan is <i>interest-free</i> for the period of time
     * indicated.
     */
    private final Integer interestChargingGrace;

    /**
     * Legacy method of support 'grace' on the charging of interest on a loan.
     * 
     * <p>
     * For the typical structured loan, its reasonable to use an integer to
     * indicate the number of 'repayment frequency' periods the 'grace' should
     * apply to but for slightly <b>irregular</b> loans where the period between
     * disbursement and the date of the 'first repayment period' isnt doest
     * match the 'repayment frequency' but can be less (15days instead of 1
     * month) or more (6 weeks instead of 1 month) - The idea was to use a date
     * to indicate from whence interest should be charged.
     * </p>
     */
    private LocalDate interestChargedFromDate;
    private final Money inArrearsTolerance;

    private final Integer graceOnArrearsAgeing;

    // added
    private LocalDate loanEndDate;

    private final List<DisbursementData> disbursementDatas;

    private final boolean multiDisburseLoan;

    private BigDecimal fixedEmiAmount;

    private BigDecimal fixedPrincipalAmount;

    private BigDecimal currentPeriodFixedEmiAmount;

    private BigDecimal currentPeriodFixedPrincipalAmount;

    private final BigDecimal actualFixedEmiAmount;

    private final BigDecimal maxOutstandingBalance;

    private Money totalInterestDue;

    private final DaysInMonthType daysInMonthType;

    private final DaysInYearType daysInYearType;

    private final boolean interestRecalculationEnabled;

    private final LoanRescheduleStrategyMethod rescheduleStrategyMethod;

    private final InterestRecalculationCompoundingMethod interestRecalculationCompoundingMethod;

    private final CalendarInstance restCalendarInstance;

    private final RecalculationFrequencyType recalculationFrequencyType;

    private final CalendarInstance compoundingCalendarInstance;

    private final RecalculationFrequencyType compoundingFrequencyType;

    private final BigDecimal principalThresholdForLastInstalment;
    private final Integer installmentAmountInMultiplesOf;

    private final LoanPreClosureInterestCalculationStrategy preClosureInterestCalculationStrategy;

    private Money approvedPrincipal = null;

    private final LoanTermVariationsDataWrapper variationsDataWrapper;

    private Money adjustPrincipalForFlatLoans;

    private final LocalDate seedDate;

    public static LoanApplicationTerms assembleFrom(final ApplicationCurrency currency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, final Integer numberOfRepayments, final Integer repaymentEvery,
            final PeriodFrequencyType repaymentPeriodFrequencyType, Integer nthDay, DayOfWeekType weekDayType,
            final AmortizationMethod amortizationMethod, final InterestMethod interestMethod, final BigDecimal interestRatePerPeriod,
            final PeriodFrequencyType interestRatePeriodFrequencyType, final BigDecimal annualNominalInterestRate,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final boolean allowPartialPeriodInterestCalcualtion,
            final Money principalMoney, final LocalDate expectedDisbursementDate, final LocalDate repaymentsStartingFromDate,
            final LocalDate calculatedRepaymentsStartingFromDate, final Integer graceOnPrincipalPayment,
            final Integer graceOnInterestPayment, final Integer graceOnInterestCharged, final LocalDate interestChargedFromDate,
            final Money inArrearsTolerance, final boolean multiDisburseLoan, final BigDecimal emiAmount,
            final List<DisbursementData> disbursementDatas, final BigDecimal maxOutstandingBalance, final Integer graceOnArrearsAgeing,
            final DaysInMonthType daysInMonthType, final DaysInYearType daysInYearType, final boolean isInterestRecalculationEnabled,
            final RecalculationFrequencyType recalculationFrequencyType, final CalendarInstance restCalendarInstance,
            final CalendarInstance compoundingCalendarInstance, final RecalculationFrequencyType compoundingFrequencyType,
            final BigDecimal principalThresholdForLastInstalment, final Integer installmentAmountInMultiplesOf,
            final LoanPreClosureInterestCalculationStrategy preClosureInterestCalculationStrategy, final Calendar loanCalendar,
            BigDecimal approvedAmount, List<LoanTermVariationsData> loanTermVariations) {

        final LoanRescheduleStrategyMethod rescheduleStrategyMethod = null;
        final InterestRecalculationCompoundingMethod interestRecalculationCompoundingMethod = null;
        return new LoanApplicationTerms(currency, loanTermFrequency, loanTermPeriodFrequencyType, numberOfRepayments, repaymentEvery,
                repaymentPeriodFrequencyType, nthDay, weekDayType, amortizationMethod, interestMethod, interestRatePerPeriod,
                interestRatePeriodFrequencyType, annualNominalInterestRate, interestCalculationPeriodMethod,
                allowPartialPeriodInterestCalcualtion, principalMoney, expectedDisbursementDate, repaymentsStartingFromDate,
                calculatedRepaymentsStartingFromDate, graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged,
                interestChargedFromDate, inArrearsTolerance, multiDisburseLoan, emiAmount, disbursementDatas, maxOutstandingBalance,
                graceOnArrearsAgeing, daysInMonthType, daysInYearType, isInterestRecalculationEnabled, rescheduleStrategyMethod,
                interestRecalculationCompoundingMethod, restCalendarInstance, recalculationFrequencyType, compoundingCalendarInstance,
                compoundingFrequencyType, principalThresholdForLastInstalment, installmentAmountInMultiplesOf,
                preClosureInterestCalculationStrategy, loanCalendar, approvedAmount, loanTermVariations);
    }

    public static LoanApplicationTerms assembleFrom(final ApplicationCurrency applicationCurrency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, NthDayType nthDay, DayOfWeekType dayOfWeek,
            final LocalDate expectedDisbursementDate, final LocalDate repaymentsStartingFromDate,
            final LocalDate calculatedRepaymentsStartingFromDate, final Money inArrearsTolerance,
            final LoanProductRelatedDetail loanProductRelatedDetail, final boolean multiDisburseLoan, final BigDecimal emiAmount,
            final List<DisbursementData> disbursementDatas, final BigDecimal maxOutstandingBalance,
            final LocalDate interestChargedFromDate, final BigDecimal principalThresholdForLastInstalment,
            final Integer installmentAmountInMultiplesOf, final RecalculationFrequencyType recalculationFrequencyType,
            final CalendarInstance restCalendarInstance, final InterestRecalculationCompoundingMethod compoundingMethod,
            final CalendarInstance compoundingCalendarInstance, final RecalculationFrequencyType compoundingFrequencyType,
            final LoanPreClosureInterestCalculationStrategy loanPreClosureInterestCalculationStrategy,
            final LoanRescheduleStrategyMethod rescheduleStrategyMethod, BigDecimal approvedAmount, BigDecimal annualNominalInterestRate,
            List<LoanTermVariationsData> loanTermVariations) {
        final Calendar loanCalendar = null;

        return assembleFrom(applicationCurrency, loanTermFrequency, loanTermPeriodFrequencyType, nthDay, dayOfWeek,
                expectedDisbursementDate, repaymentsStartingFromDate, calculatedRepaymentsStartingFromDate, inArrearsTolerance,
                loanProductRelatedDetail, multiDisburseLoan, emiAmount, disbursementDatas, maxOutstandingBalance, interestChargedFromDate,
                principalThresholdForLastInstalment, installmentAmountInMultiplesOf, recalculationFrequencyType, restCalendarInstance,
                compoundingMethod, compoundingCalendarInstance, compoundingFrequencyType, loanPreClosureInterestCalculationStrategy,
                rescheduleStrategyMethod, loanCalendar, approvedAmount, annualNominalInterestRate, loanTermVariations);
    }

    public static LoanApplicationTerms assembleFrom(final ApplicationCurrency applicationCurrency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, NthDayType nthDay, DayOfWeekType dayOfWeek,
            final LocalDate expectedDisbursementDate, final LocalDate repaymentsStartingFromDate,
            final LocalDate calculatedRepaymentsStartingFromDate, final Money inArrearsTolerance,
            final LoanProductRelatedDetail loanProductRelatedDetail, final boolean multiDisburseLoan, final BigDecimal emiAmount,
            final List<DisbursementData> disbursementDatas, final BigDecimal maxOutstandingBalance,
            final LocalDate interestChargedFromDate, final BigDecimal principalThresholdForLastInstalment,
            final Integer installmentAmountInMultiplesOf, final RecalculationFrequencyType recalculationFrequencyType,
            final CalendarInstance restCalendarInstance, final InterestRecalculationCompoundingMethod compoundingMethod,
            final CalendarInstance compoundingCalendarInstance, final RecalculationFrequencyType compoundingFrequencyType,
            final LoanPreClosureInterestCalculationStrategy loanPreClosureInterestCalculationStrategy,
            final LoanRescheduleStrategyMethod rescheduleStrategyMethod, final Calendar loanCalendar, BigDecimal approvedAmount,
            BigDecimal annualNominalInterestRate, final List<LoanTermVariationsData> loanTermVariations) {

        final Integer numberOfRepayments = loanProductRelatedDetail.getNumberOfRepayments();
        final Integer repaymentEvery = loanProductRelatedDetail.getRepayEvery();
        final PeriodFrequencyType repaymentPeriodFrequencyType = loanProductRelatedDetail.getRepaymentPeriodFrequencyType();
        final AmortizationMethod amortizationMethod = loanProductRelatedDetail.getAmortizationMethod();
        final InterestMethod interestMethod = loanProductRelatedDetail.getInterestMethod();
        final BigDecimal interestRatePerPeriod = loanProductRelatedDetail.getNominalInterestRatePerPeriod();
        final PeriodFrequencyType interestRatePeriodFrequencyType = loanProductRelatedDetail.getInterestPeriodFrequencyType();
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = loanProductRelatedDetail
                .getInterestCalculationPeriodMethod();
        final boolean allowPartialPeriodInterestCalcualtion = loanProductRelatedDetail.isAllowPartialPeriodInterestCalcualtion();
        final Money principalMoney = loanProductRelatedDetail.getPrincipal();

        //
        final Integer graceOnPrincipalPayment = loanProductRelatedDetail.graceOnPrincipalPayment();
        final Integer graceOnInterestPayment = loanProductRelatedDetail.graceOnInterestPayment();
        final Integer graceOnInterestCharged = loanProductRelatedDetail.graceOnInterestCharged();

        // Interest recalculation settings
        final DaysInMonthType daysInMonthType = loanProductRelatedDetail.fetchDaysInMonthType();
        final DaysInYearType daysInYearType = loanProductRelatedDetail.fetchDaysInYearType();
        final boolean isInterestRecalculationEnabled = loanProductRelatedDetail.isInterestRecalculationEnabled();
        return new LoanApplicationTerms(applicationCurrency, loanTermFrequency, loanTermPeriodFrequencyType, numberOfRepayments,
                repaymentEvery, repaymentPeriodFrequencyType, nthDay.getValue(), dayOfWeek, amortizationMethod, interestMethod,
                interestRatePerPeriod, interestRatePeriodFrequencyType, annualNominalInterestRate, interestCalculationPeriodMethod,
                allowPartialPeriodInterestCalcualtion, principalMoney, expectedDisbursementDate, repaymentsStartingFromDate,
                calculatedRepaymentsStartingFromDate, graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged,
                interestChargedFromDate, inArrearsTolerance, multiDisburseLoan, emiAmount, disbursementDatas, maxOutstandingBalance,
                loanProductRelatedDetail.getGraceOnDueDate(), daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                rescheduleStrategyMethod, compoundingMethod, restCalendarInstance, recalculationFrequencyType, compoundingCalendarInstance,
                compoundingFrequencyType, principalThresholdForLastInstalment, installmentAmountInMultiplesOf,
                loanPreClosureInterestCalculationStrategy, loanCalendar, approvedAmount, loanTermVariations);
    }

    public static LoanApplicationTerms assembleFrom(final ApplicationCurrency applicationCurrency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, final LocalDate expectedDisbursementDate,
            final LocalDate repaymentsStartingFromDate, final LocalDate calculatedRepaymentsStartingFromDate,
            final Money inArrearsTolerance, final LoanProductRelatedDetail loanProductRelatedDetail, final boolean multiDisburseLoan,
            final BigDecimal emiAmount, final List<DisbursementData> disbursementDatas, final BigDecimal maxOutstandingBalance,
            final LocalDate interestChargedFromDate, final LoanInterestRecalculationDetails interestRecalculationDetails,
            final CalendarInstance restCalendarInstance, final RecalculationFrequencyType recalculationFrequencyType,
            final CalendarInstance compoundingCalendarInstance, final RecalculationFrequencyType compoundingFrequencyType,
            final BigDecimal principalThresholdForLastInstalment, final Integer installmentAmountInMultiplesOf,
            final LoanPreClosureInterestCalculationStrategy loanPreClosureInterestCalculationStrategy, BigDecimal approvedAmount,
            final BigDecimal annualNominalInterestRate, final List<LoanTermVariationsData> loanTermVariations) {

        final Integer numberOfRepayments = loanProductRelatedDetail.getNumberOfRepayments();
        final Integer repaymentEvery = loanProductRelatedDetail.getRepayEvery();
        final PeriodFrequencyType repaymentPeriodFrequencyType = loanProductRelatedDetail.getRepaymentPeriodFrequencyType();
        final AmortizationMethod amortizationMethod = loanProductRelatedDetail.getAmortizationMethod();
        final InterestMethod interestMethod = loanProductRelatedDetail.getInterestMethod();
        final BigDecimal interestRatePerPeriod = loanProductRelatedDetail.getNominalInterestRatePerPeriod();
        final PeriodFrequencyType interestRatePeriodFrequencyType = loanProductRelatedDetail.getInterestPeriodFrequencyType();
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = loanProductRelatedDetail
                .getInterestCalculationPeriodMethod();
        final boolean allowPartialPeriodInterestCalcualtion = loanProductRelatedDetail.isAllowPartialPeriodInterestCalcualtion();
        final Money principalMoney = loanProductRelatedDetail.getPrincipal();

        //
        final Integer graceOnPrincipalPayment = loanProductRelatedDetail.graceOnPrincipalPayment();
        final Integer graceOnInterestPayment = loanProductRelatedDetail.graceOnInterestPayment();
        final Integer graceOnInterestCharged = loanProductRelatedDetail.graceOnInterestCharged();

        // Interest recalculation settings
        final DaysInMonthType daysInMonthType = loanProductRelatedDetail.fetchDaysInMonthType();
        final DaysInYearType daysInYearType = loanProductRelatedDetail.fetchDaysInYearType();
        final boolean isInterestRecalculationEnabled = loanProductRelatedDetail.isInterestRecalculationEnabled();
        LoanRescheduleStrategyMethod rescheduleStrategyMethod = null;
        InterestRecalculationCompoundingMethod interestRecalculationCompoundingMethod = null;
        if (isInterestRecalculationEnabled) {
            rescheduleStrategyMethod = interestRecalculationDetails.getRescheduleStrategyMethod();
            interestRecalculationCompoundingMethod = interestRecalculationDetails.getInterestRecalculationCompoundingMethod();
        }
        final Calendar loanCalendar = null;
        return new LoanApplicationTerms(applicationCurrency, loanTermFrequency, loanTermPeriodFrequencyType, numberOfRepayments,
                repaymentEvery, repaymentPeriodFrequencyType, null, null, amortizationMethod, interestMethod, interestRatePerPeriod,
                interestRatePeriodFrequencyType, annualNominalInterestRate, interestCalculationPeriodMethod,
                allowPartialPeriodInterestCalcualtion, principalMoney, expectedDisbursementDate, repaymentsStartingFromDate,
                calculatedRepaymentsStartingFromDate, graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged,
                interestChargedFromDate, inArrearsTolerance, multiDisburseLoan, emiAmount, disbursementDatas, maxOutstandingBalance,
                loanProductRelatedDetail.getGraceOnDueDate(), daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                rescheduleStrategyMethod, interestRecalculationCompoundingMethod, restCalendarInstance, recalculationFrequencyType,
                compoundingCalendarInstance, compoundingFrequencyType, principalThresholdForLastInstalment, installmentAmountInMultiplesOf,
                loanPreClosureInterestCalculationStrategy, loanCalendar, approvedAmount, loanTermVariations);
    }

    public static LoanApplicationTerms assembleFrom(final LoanApplicationTerms applicationTerms,
            final List<LoanTermVariationsData> loanTermVariations) {
        return new LoanApplicationTerms(applicationTerms.currency, applicationTerms.loanTermFrequency,
                applicationTerms.loanTermPeriodFrequencyType, applicationTerms.numberOfRepayments, applicationTerms.repaymentEvery,
                applicationTerms.repaymentPeriodFrequencyType, applicationTerms.nthDay, applicationTerms.weekDayType,
                applicationTerms.amortizationMethod, applicationTerms.interestMethod, applicationTerms.interestRatePerPeriod,
                applicationTerms.interestRatePeriodFrequencyType, applicationTerms.annualNominalInterestRate,
                applicationTerms.interestCalculationPeriodMethod, applicationTerms.allowPartialPeriodInterestCalcualtion,
                applicationTerms.principal, applicationTerms.expectedDisbursementDate, applicationTerms.repaymentsStartingFromDate,
                applicationTerms.calculatedRepaymentsStartingFromDate, applicationTerms.principalGrace,
                applicationTerms.interestPaymentGrace, applicationTerms.interestChargingGrace, applicationTerms.interestChargedFromDate,
                applicationTerms.inArrearsTolerance, applicationTerms.multiDisburseLoan, applicationTerms.actualFixedEmiAmount,
                applicationTerms.disbursementDatas, applicationTerms.maxOutstandingBalance, applicationTerms.graceOnArrearsAgeing,
                applicationTerms.daysInMonthType, applicationTerms.daysInYearType, applicationTerms.interestRecalculationEnabled,
                applicationTerms.rescheduleStrategyMethod, applicationTerms.interestRecalculationCompoundingMethod,
                applicationTerms.restCalendarInstance, applicationTerms.recalculationFrequencyType,
                applicationTerms.compoundingCalendarInstance, applicationTerms.compoundingFrequencyType,
                applicationTerms.principalThresholdForLastInstalment, applicationTerms.installmentAmountInMultiplesOf,
                applicationTerms.preClosureInterestCalculationStrategy, applicationTerms.loanCalendar,
                applicationTerms.approvedPrincipal.getAmount(), loanTermVariations);
    }

    private LoanApplicationTerms(final ApplicationCurrency currency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, final Integer numberOfRepayments, final Integer repaymentEvery,
            final PeriodFrequencyType repaymentPeriodFrequencyType, final Integer nthDay, final DayOfWeekType weekDayType,
            final AmortizationMethod amortizationMethod, final InterestMethod interestMethod, final BigDecimal interestRatePerPeriod,
            final PeriodFrequencyType interestRatePeriodFrequencyType, final BigDecimal annualNominalInterestRate,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final boolean allowPartialPeriodInterestCalcualtion,
            final Money principal, final LocalDate expectedDisbursementDate, final LocalDate repaymentsStartingFromDate,
            final LocalDate calculatedRepaymentsStartingFromDate, final Integer principalGrace, final Integer interestPaymentGrace,
            final Integer interestChargingGrace, final LocalDate interestChargedFromDate, final Money inArrearsTolerance,
            final boolean multiDisburseLoan, final BigDecimal emiAmount, final List<DisbursementData> disbursementDatas,
            final BigDecimal maxOutstandingBalance, final Integer graceOnArrearsAgeing, final DaysInMonthType daysInMonthType,
            final DaysInYearType daysInYearType, final boolean isInterestRecalculationEnabled,
            final LoanRescheduleStrategyMethod rescheduleStrategyMethod,
            final InterestRecalculationCompoundingMethod interestRecalculationCompoundingMethod,
            final CalendarInstance restCalendarInstance, final RecalculationFrequencyType recalculationFrequencyType,
            final CalendarInstance compoundingCalendarInstance, final RecalculationFrequencyType compoundingFrequencyType,
            final BigDecimal principalThresholdForLastInstalment, final Integer installmentAmountInMultiplesOf,
            final LoanPreClosureInterestCalculationStrategy preClosureInterestCalculationStrategy, final Calendar loanCalendar,
            BigDecimal approvedAmount, List<LoanTermVariationsData> loanTermVariations) {
        this.currency = currency;
        this.loanTermFrequency = loanTermFrequency;
        this.loanTermPeriodFrequencyType = loanTermPeriodFrequencyType;
        this.numberOfRepayments = numberOfRepayments;
        this.repaymentEvery = repaymentEvery;
        this.repaymentPeriodFrequencyType = repaymentPeriodFrequencyType;
        this.nthDay = nthDay;
        this.weekDayType = weekDayType;
        this.amortizationMethod = amortizationMethod;

        this.interestMethod = interestMethod;
        this.interestRatePerPeriod = interestRatePerPeriod;
        this.interestRatePeriodFrequencyType = interestRatePeriodFrequencyType;
        this.annualNominalInterestRate = annualNominalInterestRate;
        this.interestCalculationPeriodMethod = interestCalculationPeriodMethod;
        this.allowPartialPeriodInterestCalcualtion = allowPartialPeriodInterestCalcualtion;

        this.principal = principal;
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.repaymentsStartingFromDate = repaymentsStartingFromDate;
        this.calculatedRepaymentsStartingFromDate = calculatedRepaymentsStartingFromDate;

        this.principalGrace = principalGrace;
        this.interestPaymentGrace = interestPaymentGrace;
        this.interestChargingGrace = interestChargingGrace;
        this.interestChargedFromDate = interestChargedFromDate;

        this.inArrearsTolerance = inArrearsTolerance;
        this.multiDisburseLoan = multiDisburseLoan;
        this.fixedEmiAmount = emiAmount;
        this.actualFixedEmiAmount = emiAmount;
        this.disbursementDatas = disbursementDatas;
        this.maxOutstandingBalance = maxOutstandingBalance;
        this.graceOnArrearsAgeing = graceOnArrearsAgeing;
        this.daysInMonthType = daysInMonthType;
        this.daysInYearType = daysInYearType;
        this.interestRecalculationEnabled = isInterestRecalculationEnabled;
        this.rescheduleStrategyMethod = rescheduleStrategyMethod;
        this.interestRecalculationCompoundingMethod = interestRecalculationCompoundingMethod;
        this.restCalendarInstance = restCalendarInstance;
        this.compoundingCalendarInstance = compoundingCalendarInstance;
        this.recalculationFrequencyType = recalculationFrequencyType;
        this.compoundingFrequencyType = compoundingFrequencyType;
        this.principalThresholdForLastInstalment = principalThresholdForLastInstalment;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        this.preClosureInterestCalculationStrategy = preClosureInterestCalculationStrategy;

        this.loanCalendar = loanCalendar;
        this.approvedPrincipal = Money.of(principal.getCurrency(), approvedAmount);
        this.variationsDataWrapper = new LoanTermVariationsDataWrapper(loanTermVariations);
        this.actualNumberOfRepayments = numberOfRepayments + getLoanTermVariations().adjustNumberOfRepayments();
        this.adjustPrincipalForFlatLoans = principal.zero();
        if (this.calculatedRepaymentsStartingFromDate == null) {
            this.seedDate = this.expectedDisbursementDate;
        } else {
            this.seedDate = this.calculatedRepaymentsStartingFromDate;
        }
    }

    public Money adjustPrincipalIfLastRepaymentPeriod(final Money principalForPeriod, final Money totalCumulativePrincipalToDate,
            final int periodNumber) {

        Money adjusted = principalForPeriod;

        final Money totalPrincipalRemaining = this.principal.minus(totalCumulativePrincipalToDate);
        if (totalPrincipalRemaining.isLessThanZero()) {
            // paid too much principal, subtract amount that overpays from
            // principal paid for period.
            adjusted = principalForPeriod.minus(totalPrincipalRemaining.abs());
        } else if (this.actualFixedEmiAmount != null) {
            final Money difference = this.principal.minus(totalCumulativePrincipalToDate);
            final Money principalThreshold = principalForPeriod.multipliedBy(this.principalThresholdForLastInstalment).dividedBy(100,
                    MoneyHelper.getRoundingMode());
            if (difference.isLessThan(principalThreshold)) {
                adjusted = principalForPeriod.plus(difference.abs());
            }
        } else if (isLastRepaymentPeriod(this.actualNumberOfRepayments, periodNumber)) {

            final Money difference = totalCumulativePrincipalToDate.minus(this.principal);
            if (difference.isLessThanZero()) {
                adjusted = principalForPeriod.plus(difference.abs());
            } else if (difference.isGreaterThanZero()) {
                adjusted = principalForPeriod.minus(difference.abs());
            }
        }

        return adjusted;
    }

    public Money adjustInterestIfLastRepaymentPeriod(final Money interestForThisPeriod, final Money totalCumulativeInterestToDate,
            final Money totalInterestDueForLoan, final int periodNumber) {

        Money adjusted = interestForThisPeriod;

        final Money totalInterestRemaining = totalInterestDueForLoan.minus(totalCumulativeInterestToDate);
        if (totalInterestRemaining.isLessThanZero()) {
            // paid too much interest, subtract amount that overpays from
            // interest paid for period.
            adjusted = interestForThisPeriod.minus(totalInterestRemaining.abs());
        } else if (isLastRepaymentPeriod(this.actualNumberOfRepayments, periodNumber)) {
            final Money interestDifference = totalCumulativeInterestToDate.minus(totalInterestDueForLoan);
            if (interestDifference.isLessThanZero()) {
                adjusted = interestForThisPeriod.plus(interestDifference.abs());
            } else if (interestDifference.isGreaterThanZero()) {
                adjusted = interestForThisPeriod.minus(interestDifference.abs());
            }
        }
        if (adjusted.isLessThanZero()) {
            adjusted = adjusted.plus(adjusted);
        }
        return adjusted;
    }

    /**
     * Calculates the total interest to be charged on loan taking into account
     * grace settings.
     * 
     */
    public Money calculateTotalInterestCharged(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc) {

        Money totalInterestCharged = this.principal.zero();

        switch (this.interestMethod) {
            case FLAT:
                final Money totalInterestChargedForLoanTerm = calculateTotalFlatInterestDueWithoutGrace(calculator, mc);

                final Money totalInterestPerInstallment = calculateTotalInterestPerInstallmentWithoutGrace(calculator, mc);

                final Money totalGraceOnInterestCharged = totalInterestPerInstallment.multiplyRetainScale(getInterestChargingGrace(),
                        mc.getRoundingMode());

                totalInterestCharged = totalInterestChargedForLoanTerm.minus(totalGraceOnInterestCharged);
            break;
            case DECLINING_BALANCE:
            case INVALID:
            break;
        }

        return totalInterestCharged;
    }

    public Money calculateTotalPrincipalForPeriod(final PaymentPeriodsInOneYearCalculator calculator, final Money outstandingBalance,
            final int periodNumber, final MathContext mc, Money interestForThisInstallment) {

        Money principalForInstallment = this.principal.zero();

        switch (this.interestMethod) {
            case FLAT:
                principalForInstallment = calculateTotalPrincipalPerPeriodWithoutGrace(mc, periodNumber);
            break;
            case DECLINING_BALANCE:
                switch (this.amortizationMethod) {
                    case EQUAL_INSTALLMENTS:
                        Money totalPmtForThisInstallment = pmtForInstallment(calculator, outstandingBalance, periodNumber, mc);
                        principalForInstallment = calculatePrincipalDueForInstallment(periodNumber, totalPmtForThisInstallment,
                                interestForThisInstallment);
                    break;
                    case EQUAL_PRINCIPAL:
                        principalForInstallment = calculateEqualPrincipalDueForInstallment(mc, periodNumber);
                    break;
                    case INVALID:
                    break;
                }
            break;
            case INVALID:
            break;
        }

        return principalForInstallment;
    }

    public Money pmtForInstallment(final PaymentPeriodsInOneYearCalculator calculator, final Money outstandingBalance,
            final int periodNumber, final MathContext mc) {
        // Calculate exact period from disbursement date
        final LocalDate periodStartDate = getExpectedDisbursementDate().withDayOfMonth(1);
        final LocalDate periodEndDate = getPeriodEndDate(periodStartDate);
        // equal installments
        final int periodsElapsed = periodNumber - 1;
        // with periodic interest for default month and year for
        // equal installment
        final BigDecimal periodicInterestRateForRepaymentPeriod = periodicInterestRate(calculator, mc, DaysInMonthType.DAYS_30,
                DaysInYearType.DAYS_365, periodStartDate, periodEndDate);
        Money totalPmtForThisInstallment = calculateTotalDueForEqualInstallmentRepaymentPeriod(periodicInterestRateForRepaymentPeriod,
                outstandingBalance, periodsElapsed);
        return totalPmtForThisInstallment;
    }

    private LocalDate getPeriodEndDate(final LocalDate startDate) {
        LocalDate dueRepaymentPeriodDate = startDate;
        switch (this.repaymentPeriodFrequencyType) {
            case DAYS:
                dueRepaymentPeriodDate = startDate.plusDays(this.repaymentEvery);
            break;
            case WEEKS:
                dueRepaymentPeriodDate = startDate.plusWeeks(this.repaymentEvery);
            break;
            case MONTHS:
                dueRepaymentPeriodDate = startDate.plusMonths(this.repaymentEvery);
            break;
            case YEARS:
                dueRepaymentPeriodDate = startDate.plusYears(this.repaymentEvery);
            break;
            case INVALID:
            break;
        }
        return dueRepaymentPeriodDate;
    }

    public PrincipalInterest calculateTotalInterestForPeriod(final PaymentPeriodsInOneYearCalculator calculator,
            final double interestCalculationGraceOnRepaymentPeriodFraction, final int periodNumber, final MathContext mc,
            final Money cumulatingInterestPaymentDueToGrace, final Money outstandingBalance, final LocalDate periodStartDate,
            final LocalDate periodEndDate) {

        Money interestForInstallment = this.principal.zero();
        Money interestBroughtForwardDueToGrace = cumulatingInterestPaymentDueToGrace.copy();

        switch (this.interestMethod) {
            case FLAT:

                switch (this.amortizationMethod) {
                    case EQUAL_INSTALLMENTS:
                        // average out outstanding interest over remaining
                        // instalments where interest is applicable
                        interestForInstallment = calculateTotalFlatInterestForInstallmentAveragingOutGracePeriods(calculator, periodNumber,
                                mc);
                    break;
                    case EQUAL_PRINCIPAL:
                        // interest follows time-value of money and is brought
                        // forward to next applicable interest payment period
                        final PrincipalInterest result = calculateTotalFlatInterestForPeriod(calculator, periodNumber, mc,
                                interestBroughtForwardDueToGrace);
                        interestForInstallment = result.interest();
                        interestBroughtForwardDueToGrace = result.interestPaymentDueToGrace();
                    break;
                    case INVALID:
                    break;
                }
            break;
            case DECLINING_BALANCE:

                final Money interestForThisInstallmentBeforeGrace = calculateDecliningInterestDueForInstallmentBeforeApplyingGrace(
                        calculator, mc, outstandingBalance, periodStartDate, periodEndDate);

                final Money interestForThisInstallmentAfterGrace = calculateDecliningInterestDueForInstallmentAfterApplyingGrace(
                        calculator, interestCalculationGraceOnRepaymentPeriodFraction, mc, outstandingBalance, periodNumber,
                        periodStartDate, periodEndDate);

                interestForInstallment = interestForThisInstallmentAfterGrace;
                if (interestForThisInstallmentAfterGrace.isGreaterThanZero()) {
                    interestForInstallment = interestBroughtForwardDueToGrace.plus(interestForThisInstallmentAfterGrace);
                    interestBroughtForwardDueToGrace = interestBroughtForwardDueToGrace.zero();
                } else if (isInterestFreeGracePeriod(periodNumber)) {
                    interestForInstallment = interestForInstallment.zero();
                } else if (isInterestFreeGracePeriodFromDate(interestCalculationGraceOnRepaymentPeriodFraction)) {
                    interestForInstallment = interestForThisInstallmentAfterGrace;
                } else {
                    interestBroughtForwardDueToGrace = interestBroughtForwardDueToGrace.plus(interestForThisInstallmentBeforeGrace);
                }
            break;
            case INVALID:
            break;
        }

        return new PrincipalInterest(null, interestForInstallment, interestBroughtForwardDueToGrace);
    }

    private final boolean isLastRepaymentPeriod(final int numberOfRepayments, final int periodNumber) {
        return periodNumber == numberOfRepayments;
    }

    /**
     * general method to calculate totalInterestDue discounting any grace
     * settings
     */
    private Money calculateTotalFlatInterestDueWithoutGrace(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc) {

        Money totalInterestDue = this.principal.zero();

        switch (this.interestMethod) {
            case FLAT:
                final BigDecimal interestRateForLoanTerm = calculateFlatInterestRateForLoanTerm(calculator, mc);
                totalInterestDue = this.principal.multiplyRetainScale(interestRateForLoanTerm, mc.getRoundingMode());

            break;
            case DECLINING_BALANCE:
            break;
            case INVALID:
            break;
        }

        if (this.totalInterestDue != null) {
            totalInterestDue = this.totalInterestDue;
        }

        return totalInterestDue;
    }

    private BigDecimal calculateFlatInterestRateForLoanTerm(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc) {

        final BigDecimal divisor = BigDecimal.valueOf(Double.valueOf("100.0"));

        final long loanTermPeriodsInOneYear = calculatePeriodsInOneYear(calculator);
        final BigDecimal loanTermPeriodsInYearBigDecimal = BigDecimal.valueOf(loanTermPeriodsInOneYear);

        final BigDecimal loanTermFrequencyBigDecimal = calculatePeriodsInLoanTerm();

        return this.annualNominalInterestRate.divide(loanTermPeriodsInYearBigDecimal, mc).divide(divisor, mc)
                .multiply(loanTermFrequencyBigDecimal);
    }

    private BigDecimal calculatePeriodsInLoanTerm() {

        BigDecimal periodsInLoanTerm = BigDecimal.valueOf(this.loanTermFrequency);
        switch (this.interestCalculationPeriodMethod) {
            case DAILY:
                // number of days from 'ideal disbursement' to final date

                LocalDate loanStartDate = getExpectedDisbursementDate();
                if (getInterestChargedFromDate() != null && loanStartDate.isBefore(getInterestChargedFromLocalDate())) {
                    loanStartDate = getInterestChargedFromLocalDate();
                }

                final int periodsInLoanTermInteger = Days.daysBetween(loanStartDate, this.loanEndDate).getDays();
                periodsInLoanTerm = BigDecimal.valueOf(periodsInLoanTermInteger);
            break;
            case INVALID:
            break;
            case SAME_AS_REPAYMENT_PERIOD:
                LocalDate startDate = getExpectedDisbursementDate();
                periodsInLoanTerm = calculatePeriodsBetweenDates(startDate, this.loanEndDate);
            break;
        }

        return periodsInLoanTerm;
    }

    public BigDecimal calculatePeriodsBetweenDates(final LocalDate startDate, final LocalDate endDate) {
        BigDecimal numberOfPeriods = BigDecimal.ZERO;
        switch (this.repaymentPeriodFrequencyType) {
            case DAYS:
                int numberOfDays = Days.daysBetween(startDate, endDate).getDays();
                numberOfPeriods = BigDecimal.valueOf((double) numberOfDays);
            break;
            case WEEKS:
                int numberOfWeeks = Weeks.weeksBetween(startDate, endDate).getWeeks();
                int daysLeftAfterWeeks = Days.daysBetween(startDate.plusWeeks(numberOfWeeks), endDate).getDays();
                numberOfPeriods = numberOfPeriods.add(BigDecimal.valueOf(numberOfWeeks)).add(
                        BigDecimal.valueOf((double) daysLeftAfterWeeks / 7));
            break;
            case MONTHS:
                int numberOfMonths = Months.monthsBetween(startDate, endDate).getMonths();
                LocalDate startDateAfterConsideringMonths = startDate.plusMonths(numberOfMonths);
                LocalDate endDateAfterConsideringMonths = startDate.plusMonths(numberOfMonths + 1);
                int daysLeftAfterMonths = Days.daysBetween(startDateAfterConsideringMonths, endDate).getDays();
                int daysInPeriodAfterMonths = Days.daysBetween(startDateAfterConsideringMonths, endDateAfterConsideringMonths).getDays();
                numberOfPeriods = numberOfPeriods.add(BigDecimal.valueOf(numberOfMonths)).add(
                        BigDecimal.valueOf((double) daysLeftAfterMonths / daysInPeriodAfterMonths));
            break;
            case YEARS:
                int numberOfYears = Years.yearsBetween(startDate, endDate).getYears();
                LocalDate startDateAfterConsideringYears = startDate.plusYears(numberOfYears);
                LocalDate endDateAfterConsideringYears = startDate.plusYears(numberOfYears + 1);
                int daysLeftAfterYears = Days.daysBetween(startDateAfterConsideringYears, endDate).getDays();
                int daysInPeriodAfterYears = Days.daysBetween(startDateAfterConsideringYears, endDateAfterConsideringYears).getDays();
                numberOfPeriods = numberOfPeriods.add(BigDecimal.valueOf(numberOfYears)).add(
                        BigDecimal.valueOf((double) daysLeftAfterYears / daysInPeriodAfterYears));
            break;
            default:
            break;
        }
        return numberOfPeriods;
    }

    public void updateLoanEndDate(final LocalDate loanEndDate) {
        this.loanEndDate = loanEndDate;
    }

    private Money calculateTotalInterestPerInstallmentWithoutGrace(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc) {

        final Money totalInterestForLoanTerm = calculateTotalFlatInterestDueWithoutGrace(calculator, mc);

        return totalInterestForLoanTerm.dividedBy(Long.valueOf(this.actualNumberOfRepayments), mc.getRoundingMode());
    }

    private Money calculateTotalPrincipalPerPeriodWithoutGrace(final MathContext mc, final int periodNumber) {
        final int totalRepaymentsWithCapitalPayment = calculateNumberOfRepaymentsWithPrincipalPayment();
        Money principalPerPeriod = this.principal.dividedBy(totalRepaymentsWithCapitalPayment, mc.getRoundingMode()).plus(
                this.adjustPrincipalForFlatLoans);
        if (isPrincipalGraceApplicableForThisPeriod(periodNumber)) {
            principalPerPeriod = principalPerPeriod.zero();
        }
        if (!isPrincipalGraceApplicableForThisPeriod(periodNumber) && currentPeriodFixedPrincipalAmount != null) {
            this.adjustPrincipalForFlatLoans = this.adjustPrincipalForFlatLoans.plus(principalPerPeriod.minus(
                    currentPeriodFixedPrincipalAmount).dividedBy(this.actualNumberOfRepayments - periodNumber, mc.getRoundingMode()));
            principalPerPeriod = this.principal.zero().plus(currentPeriodFixedPrincipalAmount);

        }
        return principalPerPeriod;
    }

    private PrincipalInterest calculateTotalFlatInterestForPeriod(final PaymentPeriodsInOneYearCalculator calculator,
            final int periodNumber, final MathContext mc, final Money cumulatingInterestPaymentDueToGrace) {

        Money interestBroughtForwardDueToGrace = cumulatingInterestPaymentDueToGrace.copy();

        Money interestForInstallment = calculateTotalInterestPerInstallmentWithoutGrace(calculator, mc);
        if (isInterestPaymentGraceApplicableForThisPeriod(periodNumber)) {
            interestBroughtForwardDueToGrace = interestBroughtForwardDueToGrace.plus(interestForInstallment);
            interestForInstallment = interestForInstallment.zero();
        } else if (isInterestFreeGracePeriod(periodNumber)) {
            interestForInstallment = interestForInstallment.zero();
        } else if (isFirstPeriodAfterInterestPaymentGracePeriod(periodNumber)) {
            interestForInstallment = cumulatingInterestPaymentDueToGrace.plus(interestForInstallment);
            interestBroughtForwardDueToGrace = interestBroughtForwardDueToGrace.zero();
        }

        return new PrincipalInterest(null, interestForInstallment, interestBroughtForwardDueToGrace);
    }

    /*
     * calculates the interest that should be due for a given scheduled loan
     * repayment period. It takes into account GRACE periods and calculates how
     * much interest is due per period by averaging the number of periods where
     * interest is due and should be paid against the total known interest that
     * is due without grace.
     */
    private Money calculateTotalFlatInterestForInstallmentAveragingOutGracePeriods(final PaymentPeriodsInOneYearCalculator calculator,
            final int periodNumber, final MathContext mc) {

        Money interestForInstallment = calculateTotalInterestPerInstallmentWithoutGrace(calculator, mc);
        if (isInterestPaymentGraceApplicableForThisPeriod(periodNumber)) {
            interestForInstallment = interestForInstallment.zero();
        } else if (isInterestFreeGracePeriod(periodNumber)) {
            interestForInstallment = interestForInstallment.zero();
        } else {

            final Money totalInterestForLoanTerm = calculateTotalFlatInterestDueWithoutGrace(calculator, mc);

            final Money interestPerGracePeriod = calculateTotalInterestPerInstallmentWithoutGrace(calculator, mc);

            final Money totalInterestFree = interestPerGracePeriod.multipliedBy(getInterestChargingGrace());
            final Money realTotalInterestForLoan = totalInterestForLoanTerm.minus(totalInterestFree);

            final Integer interestPaymentDuePeriods = calculateNumberOfRepaymentPeriodsWhereInterestPaymentIsDue(this.actualNumberOfRepayments);

            interestForInstallment = realTotalInterestForLoan
                    .dividedBy(BigDecimal.valueOf(interestPaymentDuePeriods), mc.getRoundingMode());
        }

        return interestForInstallment;
    }

    private BigDecimal periodicInterestRate(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc,
            final DaysInMonthType daysInMonthType, final DaysInYearType daysInYearType, LocalDate periodStartDate, LocalDate periodEndDate) {

        final long loanTermPeriodsInOneYear = calculatePeriodsInOneYear(calculator);

        final BigDecimal divisor = BigDecimal.valueOf(Double.valueOf("100.0"));
        final BigDecimal loanTermPeriodsInYearBigDecimal = BigDecimal.valueOf(loanTermPeriodsInOneYear);

        BigDecimal periodicInterestRate = BigDecimal.ZERO;
        BigDecimal loanTermFrequencyBigDecimal = calculateLoanTermFrequency(periodStartDate, periodEndDate);
        switch (this.interestCalculationPeriodMethod) {
            case INVALID:
            break;
            case DAILY:
                // For daily work out number of days in the period
                BigDecimal numberOfDaysInPeriod = BigDecimal.valueOf(Days.daysBetween(periodStartDate, periodEndDate).getDays());

                final BigDecimal oneDayOfYearInterestRate = this.annualNominalInterestRate.divide(loanTermPeriodsInYearBigDecimal, mc)
                        .divide(divisor, mc);

                switch (this.repaymentPeriodFrequencyType) {
                    case INVALID:
                    break;
                    case DAYS:
                        periodicInterestRate = oneDayOfYearInterestRate.multiply(numberOfDaysInPeriod, mc);
                    break;
                    case WEEKS:
                        periodicInterestRate = oneDayOfYearInterestRate.multiply(numberOfDaysInPeriod, mc);
                    break;
                    case MONTHS:
                        if (daysInMonthType.isDaysInMonth_30()) {
                            numberOfDaysInPeriod = loanTermFrequencyBigDecimal.multiply(BigDecimal.valueOf(30), mc);
                        }
                        periodicInterestRate = oneDayOfYearInterestRate.multiply(numberOfDaysInPeriod, mc);
                    break;
                    case YEARS:
                        switch (daysInYearType) {
                            case DAYS_360:
                                numberOfDaysInPeriod = loanTermFrequencyBigDecimal.multiply(BigDecimal.valueOf(360), mc);
                            break;
                            case DAYS_364:
                                numberOfDaysInPeriod = loanTermFrequencyBigDecimal.multiply(BigDecimal.valueOf(364), mc);
                            break;
                            case DAYS_365:
                                numberOfDaysInPeriod = loanTermFrequencyBigDecimal.multiply(BigDecimal.valueOf(365), mc);
                            break;
                            default:
                            break;
                        }
                        periodicInterestRate = oneDayOfYearInterestRate.multiply(numberOfDaysInPeriod, mc);
                    break;
                }
            break;
            case SAME_AS_REPAYMENT_PERIOD:
                periodicInterestRate = this.annualNominalInterestRate.divide(loanTermPeriodsInYearBigDecimal, mc).divide(divisor, mc)
                        .multiply(loanTermFrequencyBigDecimal);
            break;
        }

        return periodicInterestRate;
    }

    private BigDecimal calculateLoanTermFrequency(final LocalDate periodStartDate, final LocalDate periodEndDate) {
        BigDecimal loanTermFrequencyBigDecimal = BigDecimal.valueOf(this.repaymentEvery);
        if (this.interestCalculationPeriodMethod.isDaily() || this.allowPartialPeriodInterestCalcualtion) {
            loanTermFrequencyBigDecimal = calculatePeriodsBetweenDates(periodStartDate, periodEndDate);
        }
        return loanTermFrequencyBigDecimal;
    }

    public BigDecimal interestRateFor(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc,
            final Money outstandingBalance, final LocalDate fromDate, final LocalDate toDate) {

        long loanTermPeriodsInOneYear = calculator.calculate(PeriodFrequencyType.DAYS).longValue();
        int repaymentEvery = Days.daysBetween(fromDate, toDate).getDays();
        if (isFallingInRepaymentPeriod(fromDate, toDate)) {
            loanTermPeriodsInOneYear = calculatePeriodsInOneYear(calculator);
            repaymentEvery = getPeriodsBetween(fromDate, toDate);
        }

        final BigDecimal divisor = BigDecimal.valueOf(Double.valueOf("100.0"));
        final BigDecimal loanTermPeriodsInYearBigDecimal = BigDecimal.valueOf(loanTermPeriodsInOneYear);
        final BigDecimal oneDayOfYearInterestRate = this.annualNominalInterestRate.divide(loanTermPeriodsInYearBigDecimal, mc).divide(
                divisor, mc);
        BigDecimal interestRate = oneDayOfYearInterestRate.multiply(BigDecimal.valueOf(repaymentEvery), mc);
        return outstandingBalance.getAmount().multiply(interestRate, mc);
    }

    private long calculatePeriodsInOneYear(final PaymentPeriodsInOneYearCalculator calculator) {

        // check if daysInYears is set if so change periodsInOneYear to days set
        // in db
        long periodsInOneYear;
        boolean daysInYearToUse = (this.repaymentPeriodFrequencyType.getCode().equalsIgnoreCase("periodFrequencyType.days") && !this.daysInYearType
                .getCode().equalsIgnoreCase("DaysInYearType.actual"));
        if (daysInYearToUse) {
            periodsInOneYear = this.daysInYearType.getValue().longValue();
        } else {
            periodsInOneYear = calculator.calculate(this.repaymentPeriodFrequencyType).longValue();
        }
        switch (this.interestCalculationPeriodMethod) {
            case DAILY:
                periodsInOneYear = (!this.daysInYearType.getCode().equalsIgnoreCase("DaysInYearType.actual")) ? this.daysInYearType
                        .getValue().longValue() : calculator.calculate(PeriodFrequencyType.DAYS).longValue();
            break;
            case INVALID:
            break;
            case SAME_AS_REPAYMENT_PERIOD:
            break;
        }

        return periodsInOneYear;
    }

    private int calculateNumberOfRepaymentsWithPrincipalPayment() {
        return this.actualNumberOfRepayments - getPrincipalGrace();
    }

    private Integer calculateNumberOfRepaymentPeriodsWhereInterestPaymentIsDue(final Integer totalNumberOfRepaymentPeriods) {
        return totalNumberOfRepaymentPeriods - Math.max(getInterestChargingGrace(), getInterestPaymentGrace());
    }

    private Integer calculateNumberOfPrincipalPaymentPeriods(final Integer totalNumberOfRepaymentPeriods) {
        return totalNumberOfRepaymentPeriods - getPrincipalGrace();
    }

    public boolean isPrincipalGraceApplicableForThisPeriod(final int periodNumber) {
        return periodNumber > 0 && periodNumber <= getPrincipalGrace();
    }

    private boolean isInterestPaymentGraceApplicableForThisPeriod(final int periodNumber) {
        return periodNumber > 0 && periodNumber <= getInterestPaymentGrace();
    }

    private boolean isFirstPeriodAfterInterestPaymentGracePeriod(final int periodNumber) {
        return periodNumber > 0 && periodNumber == getInterestPaymentGrace() + 1;
    }

    private boolean isInterestFreeGracePeriod(final int periodNumber) {
        return periodNumber > 0 && periodNumber <= getInterestChargingGrace();
    }

    public Integer getPrincipalGrace() {
        Integer graceOnPrincipalPayments = Integer.valueOf(0);
        if (this.principalGrace != null) {
            graceOnPrincipalPayments = this.principalGrace;
        }
        return graceOnPrincipalPayments;
    }

    public Integer getInterestPaymentGrace() {
        Integer graceOnInterestPayments = Integer.valueOf(0);
        if (this.interestPaymentGrace != null) {
            graceOnInterestPayments = this.interestPaymentGrace;
        }
        return graceOnInterestPayments;
    }

    public Integer getInterestChargingGrace() {
        Integer graceOnInterestCharged = Integer.valueOf(0);
        if (this.interestChargingGrace != null) {
            graceOnInterestCharged = this.interestChargingGrace;
        }
        return graceOnInterestCharged;
    }

    private double paymentPerPeriod(final BigDecimal periodicInterestRate, final Money balance, final int periodsElapsed) {

        if (getFixedEmiAmount() == null) {
            final double futureValue = 0;
            final double principalDouble = balance.getAmount().multiply(BigDecimal.valueOf(-1)).doubleValue();

            final Integer periodsRemaining = this.actualNumberOfRepayments - periodsElapsed;

            double installmentAmount = FinanicalFunctions.pmt(periodicInterestRate.doubleValue(), periodsRemaining.doubleValue(),
                    principalDouble, futureValue, false);

            if (this.installmentAmountInMultiplesOf != null) {
                installmentAmount = Money.roundToMultiplesOf(installmentAmount, this.installmentAmountInMultiplesOf);
            }
            setFixedEmiAmount(BigDecimal.valueOf(installmentAmount));
        }
        return getFixedEmiAmount().doubleValue();
    }

    private Money calculateDecliningInterestDueForInstallmentBeforeApplyingGrace(final PaymentPeriodsInOneYearCalculator calculator,
            final MathContext mc, final Money outstandingBalance, LocalDate periodStartDate, LocalDate periodEndDate) {

        Money interestDue = Money.zero(outstandingBalance.getCurrency());

        final BigDecimal periodicInterestRate = periodicInterestRate(calculator, mc, this.daysInMonthType, this.daysInYearType,
                periodStartDate, periodEndDate);
        interestDue = outstandingBalance.multiplyRetainScale(periodicInterestRate, mc.getRoundingMode());

        return interestDue;
    }

    private Money calculateDecliningInterestDueForInstallmentAfterApplyingGrace(final PaymentPeriodsInOneYearCalculator calculator,
            final double interestCalculationGraceOnRepaymentPeriodFraction, final MathContext mc, final Money outstandingBalance,
            final int periodNumber, LocalDate periodStartDate, LocalDate periodEndDate) {

        Money interest = calculateDecliningInterestDueForInstallmentBeforeApplyingGrace(calculator, mc, outstandingBalance,
                periodStartDate, periodEndDate);

        if (isInterestPaymentGraceApplicableForThisPeriod(periodNumber)) {
            interest = interest.zero();
        }

        Double fraction = interestCalculationGraceOnRepaymentPeriodFraction;

        if (isInterestFreeGracePeriod(periodNumber)) {
            interest = interest.zero();
        } else if (isInterestFreeGracePeriodFromDate(interestCalculationGraceOnRepaymentPeriodFraction)) {

            if (interestCalculationGraceOnRepaymentPeriodFraction >= Integer.valueOf(1).doubleValue()) {
                interest = interest.zero();
                fraction = fraction - Integer.valueOf(1).doubleValue();

            } else if (interestCalculationGraceOnRepaymentPeriodFraction > Double.valueOf("0.25")
                    && interestCalculationGraceOnRepaymentPeriodFraction < Integer.valueOf(1).doubleValue()) {

                final Money graceOnInterestForRepaymentPeriod = interest.multipliedBy(interestCalculationGraceOnRepaymentPeriodFraction);
                interest = interest.minus(graceOnInterestForRepaymentPeriod);
                fraction = Double.valueOf("0");
            }
        }

        return interest;
    }

    private boolean isInterestFreeGracePeriodFromDate(final double interestCalculationGraceOnRepaymentPeriodFraction) {
        return this.interestChargedFromDate != null && interestCalculationGraceOnRepaymentPeriodFraction > Double.valueOf("0.0");
    }

    private Money calculateEqualPrincipalDueForInstallment(final MathContext mc, final int periodNumber) {
        Money principal = this.principal;
        if (this.fixedPrincipalAmount == null) {
            final Integer numberOfPrincipalPaymentPeriods = calculateNumberOfPrincipalPaymentPeriods(this.actualNumberOfRepayments);
            principal = this.principal.dividedBy(numberOfPrincipalPaymentPeriods, mc.getRoundingMode());
            this.fixedPrincipalAmount = principal.getAmount();
        }
        principal = Money.of(getCurrency(), getFixedPrincipalAmount());

        if (isPrincipalGraceApplicableForThisPeriod(periodNumber)) {
            principal = principal.zero();
        }
        return principal;
    }

    public void updateFixedPrincipalAmount(final MathContext mc, final int periodNumber, final Money outstandingAmount) {
        final Integer numberOfPrincipalPaymentPeriods = calculateNumberOfPrincipalPaymentPeriods(this.actualNumberOfRepayments);
        Money principal = outstandingAmount.dividedBy(numberOfPrincipalPaymentPeriods - periodNumber + 1, mc.getRoundingMode());
        this.fixedPrincipalAmount = principal.getAmount();
    }

    public void setFixedPrincipalAmount(BigDecimal fixedPrincipalAmount) {
        this.fixedPrincipalAmount = fixedPrincipalAmount;
    }

    private Money calculatePrincipalDueForInstallment(final int periodNumber, final Money totalDuePerInstallment, final Money periodInterest) {

        Money principal = totalDuePerInstallment.minus(periodInterest);
        if (isPrincipalGraceApplicableForThisPeriod(periodNumber)) {
            principal = principal.zero();
        }
        return principal;
    }

    private Money calculateTotalDueForEqualInstallmentRepaymentPeriod(final BigDecimal periodicInterestRate, final Money balance,
            final int periodsElapsed) {

        final double paymentPerRepaymentPeriod = paymentPerPeriod(periodicInterestRate, balance, periodsElapsed);

        return Money.of(balance.getCurrency(), BigDecimal.valueOf(paymentPerRepaymentPeriod));
    }

    public LoanProductRelatedDetail toLoanProductRelatedDetail() {
        final MonetaryCurrency currency = new MonetaryCurrency(this.currency.getCode(), this.currency.getDecimalPlaces(),
                this.currency.getCurrencyInMultiplesOf());

        return LoanProductRelatedDetail.createFrom(currency, this.principal.getAmount(), this.interestRatePerPeriod,
                this.interestRatePeriodFrequencyType, this.annualNominalInterestRate, this.interestMethod,
                this.interestCalculationPeriodMethod, this.allowPartialPeriodInterestCalcualtion, this.repaymentEvery,
                this.repaymentPeriodFrequencyType, this.numberOfRepayments, this.principalGrace, this.interestPaymentGrace,
                this.interestChargingGrace, this.amortizationMethod, this.inArrearsTolerance.getAmount(), this.graceOnArrearsAgeing,
                this.daysInMonthType.getValue(), this.daysInYearType.getValue(), this.interestRecalculationEnabled);
    }

    public Integer getLoanTermFrequency() {
        return this.loanTermFrequency;
    }

    public PeriodFrequencyType getLoanTermPeriodFrequencyType() {
        return this.loanTermPeriodFrequencyType;
    }

    public Integer getRepaymentEvery() {
        return this.repaymentEvery;
    }

    public PeriodFrequencyType getRepaymentPeriodFrequencyType() {
        return this.repaymentPeriodFrequencyType;
    }

    public Date getRepaymentStartFromDate() {
        Date dateValue = null;
        if (this.repaymentsStartingFromDate != null) {
            dateValue = this.repaymentsStartingFromDate.toDate();
        }
        return dateValue;
    }

    public Date getInterestChargedFromDate() {
        Date dateValue = null;
        if (this.interestChargedFromDate != null) {
            dateValue = this.interestChargedFromDate.toDate();
        }
        return dateValue;
    }

    public void setPrincipal(Money principal) {
        this.principal = principal;
    }

    public LocalDate getInterestChargedFromLocalDate() {
        return this.interestChargedFromDate;
    }

    public InterestMethod getInterestMethod() {
        return this.interestMethod;
    }

    public AmortizationMethod getAmortizationMethod() {
        return this.amortizationMethod;
    }

    public MonetaryCurrency getCurrency() {
        return this.principal.getCurrency();
    }

    public Integer getNumberOfRepayments() {
        return this.numberOfRepayments;
    }

    public LocalDate getExpectedDisbursementDate() {
        return this.expectedDisbursementDate;
    }

    public LocalDate getRepaymentsStartingFromLocalDate() {
        return this.repaymentsStartingFromDate;
    }

    public LocalDate getCalculatedRepaymentsStartingFromLocalDate() {
        return this.calculatedRepaymentsStartingFromDate;
    }

    public Money getPrincipal() {
        return this.principal;
    }

    public Money getApprovedPrincipal() {
        return this.approvedPrincipal;
    }

    public List<DisbursementData> getDisbursementDatas() {
        return this.disbursementDatas;
    }

    public boolean isMultiDisburseLoan() {
        return this.multiDisburseLoan;
    }

    public Money getMaxOutstandingBalance() {
        return Money.of(getCurrency(), this.maxOutstandingBalance);
    }

    public BigDecimal getFixedEmiAmount() {
        BigDecimal fixedEmiAmount = this.fixedEmiAmount;
        if (getCurrentPeriodFixedEmiAmount() != null) {
            fixedEmiAmount = getCurrentPeriodFixedEmiAmount();
        }
        return fixedEmiAmount;
    }

    public Integer getNthDay() {
        return this.nthDay;
    }

    public DayOfWeekType getWeekDayType() {
        return this.weekDayType;
    }

    public void setFixedEmiAmount(BigDecimal fixedEmiAmount) {
        this.fixedEmiAmount = fixedEmiAmount;
    }

    public void resetFixedEmiAmount() {
        this.fixedEmiAmount = this.actualFixedEmiAmount;
    }

    public LoanRescheduleStrategyMethod getLoanRescheduleStrategyMethod() {
        return LoanRescheduleStrategyMethod.REDUCE_EMI_AMOUNT;
    }

    public boolean isInterestRecalculationEnabled() {
        return this.interestRecalculationEnabled;
    }

    public LoanRescheduleStrategyMethod getRescheduleStrategyMethod() {
        return this.rescheduleStrategyMethod;
    }

    public InterestRecalculationCompoundingMethod getInterestRecalculationCompoundingMethod() {
        return this.interestRecalculationCompoundingMethod;
    }

    public CalendarInstance getRestCalendarInstance() {
        return this.restCalendarInstance;
    }

    private boolean isFallingInRepaymentPeriod(LocalDate fromDate, LocalDate toDate) {
        boolean isSameAsRepaymentPeriod = false;
        if (this.interestCalculationPeriodMethod.getValue().equals(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD.getValue())) {
            switch (this.repaymentPeriodFrequencyType) {
                case WEEKS:
                    int days = Days.daysBetween(fromDate, toDate).getDays();
                    isSameAsRepaymentPeriod = (days % 7) == 0;
                break;
                case MONTHS:
                    boolean isFromDateOnEndDate = false;
                    if (fromDate.getDayOfMonth() > fromDate.plusDays(1).getDayOfMonth()) {
                        isFromDateOnEndDate = true;
                    }
                    boolean isToDateOnEndDate = false;
                    if (toDate.getDayOfMonth() > toDate.plusDays(1).getDayOfMonth()) {
                        isToDateOnEndDate = true;
                    }

                    if (isFromDateOnEndDate && isToDateOnEndDate) {
                        isSameAsRepaymentPeriod = true;
                    } else {

                        int months = getPeriodsBetween(fromDate, toDate);
                        fromDate = fromDate.plusMonths(months);
                        isSameAsRepaymentPeriod = fromDate.isEqual(toDate);
                    }

                break;
                default:
                break;
            }
        }
        return isSameAsRepaymentPeriod;
    }

    private Integer getPeriodsBetween(LocalDate fromDate, LocalDate toDate) {
        Integer numberOfPeriods = 0;
        PeriodType periodType = PeriodType.yearMonthDay();
        Period difference = new Period(fromDate, toDate, periodType);
        switch (this.repaymentPeriodFrequencyType) {
            case DAYS:
                numberOfPeriods = difference.getDays();
            break;
            case WEEKS:
                periodType = PeriodType.weeks();
                difference = new Period(fromDate, toDate, periodType);
                numberOfPeriods = difference.getWeeks();
            break;
            case MONTHS:
                numberOfPeriods = difference.getMonths();
            break;
            case YEARS:
                numberOfPeriods = difference.getYears();
            break;
            default:
            break;
        }
        return numberOfPeriods;
    }

    public RecalculationFrequencyType getRecalculationFrequencyType() {
        return this.recalculationFrequencyType;
    }

    public void updateNumberOfRepayments(final Integer numberOfRepayments) {
        this.numberOfRepayments = numberOfRepayments;
        this.actualNumberOfRepayments = numberOfRepayments + getLoanTermVariations().adjustNumberOfRepayments();

    }

    public void updatePrincipalGrace(final Integer principalGrace) {
        this.principalGrace = principalGrace;
    }

    public void updateInterestPaymentGrace(final Integer interestPaymentGrace) {
        this.interestPaymentGrace = interestPaymentGrace;
    }

    public void updateInterestRatePerPeriod(BigDecimal interestRatePerPeriod) {
        if (interestRatePerPeriod != null) {
            this.interestRatePerPeriod = interestRatePerPeriod;
        }
    }

    public void updateAnnualNominalInterestRate(BigDecimal annualNominalInterestRate) {
        if (annualNominalInterestRate != null) {
            this.annualNominalInterestRate = annualNominalInterestRate;
        }
    }

    public BigDecimal getAnnualNominalInterestRate() {
        return this.annualNominalInterestRate;
    }

    public void updateInterestChargedFromDate(LocalDate interestChargedFromDate) {
        if (interestChargedFromDate != null) {
            this.interestChargedFromDate = interestChargedFromDate;
        }
    }

    public void updateLoanTermFrequency(Integer loanTermFrequency) {
        if (loanTermFrequency != null) {
            this.loanTermFrequency = loanTermFrequency;
        }
    }

    public void updateTotalInterestDue(Money totalInterestDue) {

        if (totalInterestDue != null) {
            this.totalInterestDue = totalInterestDue;
        }
    }

    public ApplicationCurrency getApplicationCurrency() {
        return this.currency;
    }

    public InterestCalculationPeriodMethod getInterestCalculationPeriodMethod() {
        return this.interestCalculationPeriodMethod;
    }

    public LoanPreClosureInterestCalculationStrategy getPreClosureInterestCalculationStrategy() {
        return this.preClosureInterestCalculationStrategy;
    }

    public CalendarInstance getCompoundingCalendarInstance() {
        return this.compoundingCalendarInstance;
    }

    public RecalculationFrequencyType getCompoundingFrequencyType() {
        return this.compoundingFrequencyType;
    }

    public BigDecimal getActualFixedEmiAmount() {
        return this.actualFixedEmiAmount;
    }

    public Calendar getLoanCalendar() {
        return loanCalendar;
    }

    public BigDecimal getFixedPrincipalAmount() {
        BigDecimal fixedPrincipalAmount = this.fixedPrincipalAmount;
        if (getCurrentPeriodFixedPrincipalAmount() != null) {
            fixedPrincipalAmount = getCurrentPeriodFixedPrincipalAmount();
        }
        return fixedPrincipalAmount;
    }

    public LoanTermVariationsDataWrapper getLoanTermVariations() {
        return this.variationsDataWrapper;
    }

    public BigDecimal getCurrentPeriodFixedEmiAmount() {
        return this.currentPeriodFixedEmiAmount;
    }

    public void setCurrentPeriodFixedEmiAmount(BigDecimal currentPeriodFixedEmiAmount) {
        this.currentPeriodFixedEmiAmount = currentPeriodFixedEmiAmount;
    }

    public BigDecimal getCurrentPeriodFixedPrincipalAmount() {
        return this.currentPeriodFixedPrincipalAmount;
    }

    public void setCurrentPeriodFixedPrincipalAmount(BigDecimal currentPeriodFixedPrincipalAmount) {
        this.currentPeriodFixedPrincipalAmount = currentPeriodFixedPrincipalAmount;
    }

    public Integer fetchNumberOfRepaymentsAfterExceptions() {
        return this.actualNumberOfRepayments;
    }

    public LocalDate getSeedDate() {
        return this.seedDate;
    }

}