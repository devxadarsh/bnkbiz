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

import static com.stellar.bnkbiz.portfolio.savings.DepositsApiConstants.adjustAdvanceTowardsFuturePaymentsParamName;
import static com.stellar.bnkbiz.portfolio.savings.DepositsApiConstants.allowWithdrawalParamName;
import static com.stellar.bnkbiz.portfolio.savings.DepositsApiConstants.chartsParamName;
import static com.stellar.bnkbiz.portfolio.savings.DepositsApiConstants.depositAmountParamName;
import static com.stellar.bnkbiz.portfolio.savings.DepositsApiConstants.depositMaxAmountParamName;
import static com.stellar.bnkbiz.portfolio.savings.DepositsApiConstants.depositMinAmountParamName;
import static com.stellar.bnkbiz.portfolio.savings.DepositsApiConstants.inMultiplesOfDepositTermParamName;
import static com.stellar.bnkbiz.portfolio.savings.DepositsApiConstants.inMultiplesOfDepositTermTypeIdParamName;
import static com.stellar.bnkbiz.portfolio.savings.DepositsApiConstants.isMandatoryDepositParamName;
import static com.stellar.bnkbiz.portfolio.savings.DepositsApiConstants.maxDepositTermParamName;
import static com.stellar.bnkbiz.portfolio.savings.DepositsApiConstants.maxDepositTermTypeIdParamName;
import static com.stellar.bnkbiz.portfolio.savings.DepositsApiConstants.minDepositTermParamName;
import static com.stellar.bnkbiz.portfolio.savings.DepositsApiConstants.minDepositTermTypeIdParamName;
import static com.stellar.bnkbiz.portfolio.savings.DepositsApiConstants.preClosurePenalApplicableParamName;
import static com.stellar.bnkbiz.portfolio.savings.DepositsApiConstants.preClosurePenalInterestOnTypeIdParamName;
import static com.stellar.bnkbiz.portfolio.savings.DepositsApiConstants.preClosurePenalInterestParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.chargesParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.currencyCodeParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.descriptionParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.digitsAfterDecimalParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.idParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.inMultiplesOfParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.interestCalculationTypeParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.interestPostingPeriodTypeParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.minBalanceForInterestCalculationParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.nameParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.nominalAnnualInterestRateParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.shortNameParamName;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import com.stellar.bnkbiz.accounting.common.AccountingRuleType;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.organisation.monetary.domain.MonetaryCurrency;
import com.stellar.bnkbiz.portfolio.charge.domain.Charge;
import com.stellar.bnkbiz.portfolio.charge.domain.ChargeRepositoryWrapper;
import com.stellar.bnkbiz.portfolio.charge.exception.ChargeCannotBeAppliedToException;
import com.stellar.bnkbiz.portfolio.interestratechart.domain.InterestRateChart;
import com.stellar.bnkbiz.portfolio.interestratechart.service.InterestRateChartAssembler;
import com.stellar.bnkbiz.portfolio.loanproduct.exception.InvalidCurrencyException;
import com.stellar.bnkbiz.portfolio.savings.PreClosurePenalInterestOnType;
import com.stellar.bnkbiz.portfolio.savings.SavingsCompoundingInterestPeriodType;
import com.stellar.bnkbiz.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import com.stellar.bnkbiz.portfolio.savings.SavingsInterestCalculationType;
import com.stellar.bnkbiz.portfolio.savings.SavingsPeriodFrequencyType;
import com.stellar.bnkbiz.portfolio.savings.SavingsPostingInterestPeriodType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service
public class DepositProductAssembler {

    private final ChargeRepositoryWrapper chargeRepository;
    private final InterestRateChartAssembler chartAssembler;

    @Autowired
    public DepositProductAssembler(final ChargeRepositoryWrapper chargeRepository, final InterestRateChartAssembler chartAssembler) {
        this.chargeRepository = chargeRepository;
        this.chartAssembler = chartAssembler;
    }

    public FixedDepositProduct assembleFixedDepositProduct(final JsonCommand command) {

        final String name = command.stringValueOfParameterNamed(nameParamName);
        final String shortName = command.stringValueOfParameterNamed(shortNameParamName);
        final String description = command.stringValueOfParameterNamed(descriptionParamName);

        final String currencyCode = command.stringValueOfParameterNamed(currencyCodeParamName);
        final Integer digitsAfterDecimal = command.integerValueOfParameterNamed(digitsAfterDecimalParamName);
        final Integer inMultiplesOf = command.integerValueOfParameterNamed(inMultiplesOfParamName);
        final MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf);

        BigDecimal interestRate = command.bigDecimalValueOfParameterNamed(nominalAnnualInterestRateParamName);

        SavingsCompoundingInterestPeriodType interestCompoundingPeriodType = null;
        final Integer interestPeriodTypeValue = command.integerValueOfParameterNamed(interestCompoundingPeriodTypeParamName);
        if (interestPeriodTypeValue != null) {
            interestCompoundingPeriodType = SavingsCompoundingInterestPeriodType.fromInt(interestPeriodTypeValue);
        }

        SavingsPostingInterestPeriodType interestPostingPeriodType = null;
        final Integer interestPostingPeriodTypeValue = command.integerValueOfParameterNamed(interestPostingPeriodTypeParamName);
        if (interestPostingPeriodTypeValue != null) {
            interestPostingPeriodType = SavingsPostingInterestPeriodType.fromInt(interestPostingPeriodTypeValue);
        }

        SavingsInterestCalculationType interestCalculationType = null;
        final Integer interestCalculationTypeValue = command.integerValueOfParameterNamed(interestCalculationTypeParamName);
        if (interestCalculationTypeValue != null) {
            interestCalculationType = SavingsInterestCalculationType.fromInt(interestCalculationTypeValue);
        }

        SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType = null;
        final Integer interestCalculationDaysInYearTypeValue = command
                .integerValueOfParameterNamed(interestCalculationDaysInYearTypeParamName);
        if (interestCalculationDaysInYearTypeValue != null) {
            interestCalculationDaysInYearType = SavingsInterestCalculationDaysInYearType.fromInt(interestCalculationDaysInYearTypeValue);
        }

        final Integer lockinPeriodFrequency = command.integerValueOfParameterNamedDefaultToNullIfZero(lockinPeriodFrequencyParamName);
        SavingsPeriodFrequencyType lockinPeriodFrequencyType = null;
        final Integer lockinPeriodFrequencyTypeValue = command.integerValueOfParameterNamed(lockinPeriodFrequencyTypeParamName);
        if (lockinPeriodFrequencyTypeValue != null) {
            lockinPeriodFrequencyType = SavingsPeriodFrequencyType.fromInt(lockinPeriodFrequencyTypeValue);
        }

        final BigDecimal minBalanceForInterestCalculation = command
                .bigDecimalValueOfParameterNamedDefaultToNullIfZero(minBalanceForInterestCalculationParamName);

        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(command.integerValueOfParameterNamed("accountingRule"));

        final DepositPreClosureDetail preClosureDetail = this.assemblePreClosureDetail(command);
        final DepositTermDetail depositTermDetail = this.assembleDepositTermDetail(command);
        final DepositProductAmountDetails depositProductAmountDetails = this.assembleDepositAmountDetails(command);
        final DepositProductTermAndPreClosure productTermAndPreClosure = DepositProductTermAndPreClosure.createNew(preClosureDetail,
                depositTermDetail, depositProductAmountDetails, null);

        // Savings product charges
        final Set<Charge> charges = assembleListOfSavingsProductCharges(command, currencyCode);
        // Interest rate charts
        final Set<InterestRateChart> charts = assembleListOfCharts(command, currency.getCode());
        if (interestRate == null) {
            interestRate = BigDecimal.ZERO;
        }
        FixedDepositProduct fixedDepositProduct = FixedDepositProduct.createNew(name, shortName, description, currency, interestRate,
                interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType,
                lockinPeriodFrequency, lockinPeriodFrequencyType, accountingRuleType, charges, productTermAndPreClosure, charts,
                minBalanceForInterestCalculation);

        // update product reference
        productTermAndPreClosure.updateProductReference(fixedDepositProduct);

        fixedDepositProduct.validateDomainRules();

        return fixedDepositProduct;
    }

    public RecurringDepositProduct assembleRecurringDepositProduct(final JsonCommand command) {

        final String name = command.stringValueOfParameterNamed(nameParamName);
        final String shortName = command.stringValueOfParameterNamed(shortNameParamName);
        final String description = command.stringValueOfParameterNamed(descriptionParamName);

        final String currencyCode = command.stringValueOfParameterNamed(currencyCodeParamName);
        final Integer digitsAfterDecimal = command.integerValueOfParameterNamed(digitsAfterDecimalParamName);
        final Integer inMultiplesOf = command.integerValueOfParameterNamed(inMultiplesOfParamName);
        final MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf);

        BigDecimal interestRate = command.bigDecimalValueOfParameterNamed(nominalAnnualInterestRateParamName);

        SavingsCompoundingInterestPeriodType interestCompoundingPeriodType = null;
        final Integer interestPeriodTypeValue = command.integerValueOfParameterNamed(interestCompoundingPeriodTypeParamName);
        if (interestPeriodTypeValue != null) {
            interestCompoundingPeriodType = SavingsCompoundingInterestPeriodType.fromInt(interestPeriodTypeValue);
        }

        SavingsPostingInterestPeriodType interestPostingPeriodType = null;
        final Integer interestPostingPeriodTypeValue = command.integerValueOfParameterNamed(interestPostingPeriodTypeParamName);
        if (interestPostingPeriodTypeValue != null) {
            interestPostingPeriodType = SavingsPostingInterestPeriodType.fromInt(interestPostingPeriodTypeValue);
        }

        SavingsInterestCalculationType interestCalculationType = null;
        final Integer interestCalculationTypeValue = command.integerValueOfParameterNamed(interestCalculationTypeParamName);
        if (interestCalculationTypeValue != null) {
            interestCalculationType = SavingsInterestCalculationType.fromInt(interestCalculationTypeValue);
        }

        SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType = null;
        final Integer interestCalculationDaysInYearTypeValue = command
                .integerValueOfParameterNamed(interestCalculationDaysInYearTypeParamName);
        if (interestCalculationDaysInYearTypeValue != null) {
            interestCalculationDaysInYearType = SavingsInterestCalculationDaysInYearType.fromInt(interestCalculationDaysInYearTypeValue);
        }

        final Integer lockinPeriodFrequency = command.integerValueOfParameterNamedDefaultToNullIfZero(lockinPeriodFrequencyParamName);
        SavingsPeriodFrequencyType lockinPeriodFrequencyType = null;
        final Integer lockinPeriodFrequencyTypeValue = command.integerValueOfParameterNamed(lockinPeriodFrequencyTypeParamName);
        if (lockinPeriodFrequencyTypeValue != null) {
            lockinPeriodFrequencyType = SavingsPeriodFrequencyType.fromInt(lockinPeriodFrequencyTypeValue);
        }

        final BigDecimal minBalanceForInterestCalculation = command
                .bigDecimalValueOfParameterNamedDefaultToNullIfZero(minBalanceForInterestCalculationParamName);

        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(command.integerValueOfParameterNamed("accountingRule"));

        final DepositPreClosureDetail preClosureDetail = this.assemblePreClosureDetail(command);
        final DepositTermDetail depositTermDetail = this.assembleDepositTermDetail(command);
        final DepositProductAmountDetails depositProductAmountDetails = this.assembleDepositAmountDetails(command);
        final DepositProductTermAndPreClosure productTermAndPreClosure = DepositProductTermAndPreClosure.createNew(preClosureDetail,
                depositTermDetail, depositProductAmountDetails, null);
        final DepositRecurringDetail recurringDetail = this.assembleRecurringDetail(command);
        final DepositProductRecurringDetail productRecurringDetail = DepositProductRecurringDetail.createNew(recurringDetail, null);

        // Savings product charges
        final Set<Charge> charges = assembleListOfSavingsProductCharges(command, currencyCode);
        // Interest rate charts
        final Set<InterestRateChart> charts = assembleListOfCharts(command, currency.getCode());

        if (interestRate == null) {
            interestRate = BigDecimal.ZERO;
        }

        RecurringDepositProduct recurringDepositProduct = RecurringDepositProduct.createNew(name, shortName, description, currency,
                interestRate, interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType,
                interestCalculationDaysInYearType, lockinPeriodFrequency, lockinPeriodFrequencyType, accountingRuleType, charges,
                productTermAndPreClosure, productRecurringDetail, charts, minBalanceForInterestCalculation);

        // update product reference
        productTermAndPreClosure.updateProductReference(recurringDepositProduct);
        productRecurringDetail.updateProductReference(recurringDepositProduct);

        recurringDepositProduct.validateDomainRules();

        return recurringDepositProduct;
    }

    public DepositPreClosureDetail assemblePreClosureDetail(final JsonCommand command) {

        boolean preClosurePenalApplicable = false;
        BigDecimal preClosurePenalInterest = null;
        PreClosurePenalInterestOnType preClosurePenalInterestType = null;

        if (command.parameterExists(preClosurePenalApplicableParamName)) {
            preClosurePenalApplicable = command.booleanObjectValueOfParameterNamed(preClosurePenalApplicableParamName);
            if (preClosurePenalApplicable) {
                preClosurePenalInterest = command.bigDecimalValueOfParameterNamed(preClosurePenalInterestParamName);
                final Integer preClosurePenalInterestOnTypeId = command
                        .integerValueOfParameterNamed(preClosurePenalInterestOnTypeIdParamName);
                preClosurePenalInterestType = preClosurePenalInterestOnTypeId == null ? null : PreClosurePenalInterestOnType
                        .fromInt(preClosurePenalInterestOnTypeId);
            }
        }

        DepositPreClosureDetail preClosureDetail = DepositPreClosureDetail.createFrom(preClosurePenalApplicable, preClosurePenalInterest,
                preClosurePenalInterestType);

        return preClosureDetail;
    }

    public DepositPreClosureDetail assemblePreClosureDetail(final JsonCommand command, DepositPreClosureDetail produPreClosureDetail) {
        boolean preClosurePenalApplicable = false;
        BigDecimal preClosurePenalInterest = null;
        PreClosurePenalInterestOnType preClosurePenalInterestType = null;
        Integer preClosurePenalInterestOnTypeId = null;
        if (command.parameterExists(preClosurePenalApplicableParamName)) {
            preClosurePenalApplicable = command.booleanObjectValueOfParameterNamed(preClosurePenalApplicableParamName);
            if (preClosurePenalApplicable) {
                if (command.parameterExists(preClosurePenalInterestParamName)) {
                    preClosurePenalInterest = command.bigDecimalValueOfParameterNamed(preClosurePenalInterestParamName);
                } else {
                    preClosurePenalInterest = produPreClosureDetail.preClosurePenalInterest();
                }

                if (command.parameterExists(preClosurePenalInterestParamName)) {
                    preClosurePenalInterestOnTypeId = command.integerValueOfParameterNamed(preClosurePenalInterestOnTypeIdParamName);
                } else {
                    preClosurePenalInterestOnTypeId = produPreClosureDetail.preClosurePenalInterestOnTypeId();
                }
            }
        } else {
            preClosurePenalApplicable = produPreClosureDetail.preClosurePenalApplicable();
            preClosurePenalInterest = produPreClosureDetail.preClosurePenalInterest();
            preClosurePenalInterestOnTypeId = produPreClosureDetail.preClosurePenalInterestOnTypeId();
        }

        preClosurePenalInterestType = preClosurePenalInterestOnTypeId == null ? null : PreClosurePenalInterestOnType
                .fromInt(preClosurePenalInterestOnTypeId);

        DepositPreClosureDetail preClosureDetail1 = DepositPreClosureDetail.createFrom(preClosurePenalApplicable, preClosurePenalInterest,
                preClosurePenalInterestType);

        return preClosureDetail1;
    }

    public DepositTermDetail assembleDepositTermDetail(final JsonCommand command) {

        final Integer minDepositTerm = command.integerValueOfParameterNamed(minDepositTermParamName);
        final Integer maxDepositTerm = command.integerValueOfParameterNamed(maxDepositTermParamName);
        final Integer minDepositTermTypeId = command.integerValueOfParameterNamed(minDepositTermTypeIdParamName);
        final SavingsPeriodFrequencyType minDepositTermType = (minDepositTermTypeId == null) ? null : SavingsPeriodFrequencyType
                .fromInt(minDepositTermTypeId);
        final Integer maxDepositTermTypeId = command.integerValueOfParameterNamed(maxDepositTermTypeIdParamName);
        final SavingsPeriodFrequencyType maxDepositTermType = (maxDepositTermTypeId == null) ? null : SavingsPeriodFrequencyType
                .fromInt(maxDepositTermTypeId);
        final Integer inMultiplesOfDepositTerm = command.integerValueOfParameterNamed(inMultiplesOfDepositTermParamName);
        final Integer inMultiplesOfDepositTermTypeId = command.integerValueOfParameterNamed(inMultiplesOfDepositTermTypeIdParamName);
        final SavingsPeriodFrequencyType inMultiplesOfDepositTermType = (inMultiplesOfDepositTermTypeId == null) ? null
                : SavingsPeriodFrequencyType.fromInt(inMultiplesOfDepositTermTypeId);

        final DepositTermDetail depositTermDetail = DepositTermDetail.createFrom(minDepositTerm, maxDepositTerm, minDepositTermType,
                maxDepositTermType, inMultiplesOfDepositTerm, inMultiplesOfDepositTermType);

        return depositTermDetail;
    }

    public DepositTermDetail assembleDepositTermDetail(final JsonCommand command, final DepositTermDetail prodDepositTermDetail) {

        Integer minDepositTerm = null;
        Integer maxDepositTerm = null;
        Integer minDepositTermTypeId = null;
        Integer maxDepositTermTypeId = null;
        Integer inMultiplesOfDepositTerm = null;
        Integer inMultiplesOfDepositTermTypeId = null;

        if (command.parameterExists(minDepositTermParamName)) {
            minDepositTerm = command.integerValueOfParameterNamed(minDepositTermParamName);
        } else if (prodDepositTermDetail != null) {
            minDepositTerm = prodDepositTermDetail.minDepositTerm();
        }

        if (command.parameterExists(maxDepositTermParamName)) {
            maxDepositTerm = command.integerValueOfParameterNamed(maxDepositTermParamName);
        } else if (prodDepositTermDetail != null) {
            maxDepositTerm = prodDepositTermDetail.maxDepositTerm();
        }

        if (command.parameterExists(minDepositTermTypeIdParamName)) {
            minDepositTermTypeId = command.integerValueOfParameterNamed(minDepositTermTypeIdParamName);
        } else if (prodDepositTermDetail != null) {
            minDepositTermTypeId = prodDepositTermDetail.minDepositTermType();
        }

        if (command.parameterExists(maxDepositTermTypeIdParamName)) {
            maxDepositTermTypeId = command.integerValueOfParameterNamed(maxDepositTermTypeIdParamName);
        } else if (prodDepositTermDetail != null) {
            maxDepositTermTypeId = prodDepositTermDetail.maxDepositTermType();
        }

        final SavingsPeriodFrequencyType minDepositTermType = (minDepositTermTypeId == null) ? null : SavingsPeriodFrequencyType
                .fromInt(minDepositTermTypeId);

        final SavingsPeriodFrequencyType maxDepositTermType = (maxDepositTermTypeId == null) ? null : SavingsPeriodFrequencyType
                .fromInt(maxDepositTermTypeId);

        if (command.parameterExists(inMultiplesOfDepositTermParamName)) {
            inMultiplesOfDepositTerm = command.integerValueOfParameterNamed(inMultiplesOfDepositTermParamName);
        } else if (prodDepositTermDetail != null) {
            inMultiplesOfDepositTerm = prodDepositTermDetail.inMultiplesOfDepositTerm();
        }

        if (command.parameterExists(preClosurePenalApplicableParamName)) {
            inMultiplesOfDepositTermTypeId = command.integerValueOfParameterNamed(inMultiplesOfDepositTermTypeIdParamName);
        } else if (prodDepositTermDetail != null) {
            inMultiplesOfDepositTermTypeId = prodDepositTermDetail.inMultiplesOfDepositTermType();
        }

        final SavingsPeriodFrequencyType inMultiplesOfDepositTermType = (inMultiplesOfDepositTermTypeId == null) ? null
                : SavingsPeriodFrequencyType.fromInt(inMultiplesOfDepositTermTypeId);

        final DepositTermDetail depositTermDetail = DepositTermDetail.createFrom(minDepositTerm, maxDepositTerm, minDepositTermType,
                maxDepositTermType, inMultiplesOfDepositTerm, inMultiplesOfDepositTermType);

        return depositTermDetail;
    }

    public DepositRecurringDetail assembleRecurringDetail(final JsonCommand command) {

        Boolean isMandatoryDeposit = command.booleanObjectValueOfParameterNamed(isMandatoryDepositParamName);
        Boolean allowWithdrawal = command.booleanObjectValueOfParameterNamed(allowWithdrawalParamName);
        Boolean adjustAdvanceTowardsFuturePayments = command
                .booleanObjectValueOfParameterNamed(adjustAdvanceTowardsFuturePaymentsParamName);

        if (isMandatoryDeposit == null) isMandatoryDeposit = false;
        if (allowWithdrawal == null) allowWithdrawal = false;
        if (adjustAdvanceTowardsFuturePayments == null) adjustAdvanceTowardsFuturePayments = false;

        final DepositRecurringDetail depositRecurringDetail = DepositRecurringDetail.createFrom(isMandatoryDeposit, allowWithdrawal,
                adjustAdvanceTowardsFuturePayments);

        return depositRecurringDetail;
    }

    public Set<Charge> assembleListOfSavingsProductCharges(final JsonCommand command, final String savingsProductCurrencyCode) {

        final Set<Charge> charges = new HashSet<>();

        if (command.parameterExists(chargesParamName)) {
            final JsonArray chargesArray = command.arrayOfParameterNamed(chargesParamName);
            if (chargesArray != null) {
                for (int i = 0; i < chargesArray.size(); i++) {

                    final JsonObject jsonObject = chargesArray.get(i).getAsJsonObject();
                    if (jsonObject.has(idParamName)) {
                        final Long id = jsonObject.get(idParamName).getAsLong();

                        final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(id);

                        if (!charge.isSavingsCharge()) {
                            final String errorMessage = "Charge with identifier " + charge.getId()
                                    + " cannot be applied to Savings product.";
                            throw new ChargeCannotBeAppliedToException("savings.product", errorMessage, charge.getId());
                        }

                        if (!savingsProductCurrencyCode.equals(charge.getCurrencyCode())) {
                            final String errorMessage = "Charge and Savings Product must have the same currency.";
                            throw new InvalidCurrencyException("charge", "attach.to.savings.product", errorMessage);
                        }
                        charges.add(charge);
                    }
                }
            }
        }

        return charges;
    }

    private Set<InterestRateChart> assembleListOfCharts(JsonCommand command, String currencyCode) {
        final Set<InterestRateChart> charts = new HashSet<>();

        if (command.parameterExists(chartsParamName)) {
            final JsonArray chartsArray = command.arrayOfParameterNamed(chartsParamName);
            if (chartsArray != null) {
                for (int i = 0; i < chartsArray.size(); i++) {
                    final JsonObject interstRateChartElement = chartsArray.get(i).getAsJsonObject();
                    InterestRateChart chart = this.chartAssembler.assembleFrom(interstRateChartElement, currencyCode);
                    charts.add(chart);
                }
            }
        }
        return charts;
    }

    public DepositProductAmountDetails assembleDepositAmountDetails(final JsonCommand command) {

        BigDecimal minDepositAmount = null;
        if (command.parameterExists(depositMinAmountParamName)) {
            minDepositAmount = command.bigDecimalValueOfParameterNamed(depositMinAmountParamName);
        }

        BigDecimal maxDepositAmount = null;
        if (command.parameterExists(depositMaxAmountParamName)) {
            maxDepositAmount = command.bigDecimalValueOfParameterNamed(depositMaxAmountParamName);
        }

        BigDecimal depositAmount = null;
        if (command.parameterExists(depositAmountParamName)) {
            depositAmount = command.bigDecimalValueOfParameterNamed(depositAmountParamName);
        }

        final DepositProductAmountDetails depositRecurringDetail = new DepositProductAmountDetails(minDepositAmount, depositAmount,
                maxDepositAmount);

        return depositRecurringDetail;
    }
}