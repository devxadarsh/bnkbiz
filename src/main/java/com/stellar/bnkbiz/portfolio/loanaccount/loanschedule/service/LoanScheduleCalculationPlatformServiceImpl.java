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
package com.stellar.bnkbiz.portfolio.loanaccount.loanschedule.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.stellar.bnkbiz.infrastructure.configuration.domain.ConfigurationDomainService;
import com.stellar.bnkbiz.infrastructure.core.api.JsonQuery;
import com.stellar.bnkbiz.infrastructure.core.data.ApiParameterError;
import com.stellar.bnkbiz.infrastructure.core.data.DataValidatorBuilder;
import com.stellar.bnkbiz.infrastructure.core.exception.PlatformApiDataValidationException;
import com.stellar.bnkbiz.infrastructure.core.serialization.FromJsonHelper;
import com.stellar.bnkbiz.infrastructure.core.service.DateUtils;
import com.stellar.bnkbiz.organisation.monetary.data.CurrencyData;
import com.stellar.bnkbiz.organisation.monetary.domain.MonetaryCurrency;
import com.stellar.bnkbiz.organisation.monetary.domain.Money;
import com.stellar.bnkbiz.organisation.monetary.service.CurrencyReadPlatformService;
import com.stellar.bnkbiz.portfolio.accountdetails.domain.AccountType;
import com.stellar.bnkbiz.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.Loan;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.LoanDisbursementDetails;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.LoanTransaction;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import com.stellar.bnkbiz.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import com.stellar.bnkbiz.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import com.stellar.bnkbiz.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import com.stellar.bnkbiz.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import com.stellar.bnkbiz.portfolio.loanaccount.serialization.CalculateLoanScheduleQueryFromApiJsonHelper;
import com.stellar.bnkbiz.portfolio.loanaccount.serialization.LoanApplicationCommandFromApiJsonHelper;
import com.stellar.bnkbiz.portfolio.loanaccount.service.LoanAssembler;
import com.stellar.bnkbiz.portfolio.loanaccount.service.LoanReadPlatformService;
import com.stellar.bnkbiz.portfolio.loanaccount.service.LoanUtilService;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.LoanProduct;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.LoanProductRepository;
import com.stellar.bnkbiz.portfolio.loanproduct.exception.LoanProductNotFoundException;
import com.stellar.bnkbiz.portfolio.loanproduct.serialization.LoanProductDataValidator;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanScheduleCalculationPlatformServiceImpl implements LoanScheduleCalculationPlatformService {

    private final CalculateLoanScheduleQueryFromApiJsonHelper fromApiJsonDeserializer;
    private final LoanScheduleAssembler loanScheduleAssembler;
    private final FromJsonHelper fromJsonHelper;
    private final LoanProductRepository loanProductRepository;
    private final LoanProductDataValidator loanProductCommandFromApiJsonDeserializer;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanApplicationCommandFromApiJsonHelper loanApiJsonDeserializer;
    private final LoanAssembler loanAssembler;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final ConfigurationDomainService configurationDomainService;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final LoanUtilService loanUtilService;

    @Autowired
    public LoanScheduleCalculationPlatformServiceImpl(final CalculateLoanScheduleQueryFromApiJsonHelper fromApiJsonDeserializer,
            final LoanScheduleAssembler loanScheduleAssembler, final FromJsonHelper fromJsonHelper,
            final LoanProductRepository loanProductRepository, final LoanProductDataValidator loanProductCommandFromApiJsonDeserializer,
            final LoanReadPlatformService loanReadPlatformService, final LoanApplicationCommandFromApiJsonHelper loanApiJsonDeserializer,
            final LoanAssembler loanAssembler,
            final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            final ConfigurationDomainService configurationDomainService, final CurrencyReadPlatformService currencyReadPlatformService,
            final LoanUtilService loanUtilService) {
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.loanScheduleAssembler = loanScheduleAssembler;
        this.fromJsonHelper = fromJsonHelper;
        this.loanProductRepository = loanProductRepository;
        this.loanProductCommandFromApiJsonDeserializer = loanProductCommandFromApiJsonDeserializer;
        this.loanReadPlatformService = loanReadPlatformService;
        this.loanApiJsonDeserializer = loanApiJsonDeserializer;
        this.loanAssembler = loanAssembler;
        this.loanRepaymentScheduleTransactionProcessorFactory = loanRepaymentScheduleTransactionProcessorFactory;
        this.configurationDomainService = configurationDomainService;
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.loanUtilService = loanUtilService;
    }

    @Override
    public LoanScheduleModel calculateLoanSchedule(final JsonQuery query, Boolean validateParams) {

        /***
         * TODO: Vishwas, this is probably not required, test and remove the
         * same
         **/
        final Long productId = this.fromJsonHelper.extractLongNamed("productId", query.parsedJson());
        final LoanProduct loanProduct = this.loanProductRepository.findOne(productId);
        if (loanProduct == null) { throw new LoanProductNotFoundException(productId); }

        if (validateParams) {
            boolean isMeetingMandatoryForJLGLoans = configurationDomainService.isMeetingMandatoryForJLGLoans();
            this.loanApiJsonDeserializer.validateForCreate(query.json(), isMeetingMandatoryForJLGLoans, loanProduct);
        }
        this.fromApiJsonDeserializer.validate(query.json());

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        if (loanProduct.useBorrowerCycle()) {
            final Long clientId = this.fromJsonHelper.extractLongNamed("clientId", query.parsedJson());
            final Long groupId = this.fromJsonHelper.extractLongNamed("groupId", query.parsedJson());
            Integer cycleNumber = 0;
            if (clientId != null) {
                cycleNumber = this.loanReadPlatformService.retriveLoanCounter(clientId, loanProduct.getId());
            } else if (groupId != null) {
                cycleNumber = this.loanReadPlatformService.retriveLoanCounter(groupId, AccountType.GROUP.getValue(), loanProduct.getId());
            }
            this.loanProductCommandFromApiJsonDeserializer.validateMinMaxConstraints(query.parsedJson(), baseDataValidator, loanProduct,
                    cycleNumber);
        } else {
            this.loanProductCommandFromApiJsonDeserializer.validateMinMaxConstraints(query.parsedJson(), baseDataValidator, loanProduct);
        }
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }

        return this.loanScheduleAssembler.assembleLoanScheduleFrom(query.parsedJson());
    }

    @Override
    public void updateFutureSchedule(LoanScheduleData loanScheduleData, final Long loanId) {

        final Loan loan = this.loanAssembler.assembleFrom(loanId);

        LocalDate today = DateUtils.getLocalDateOfTenant();
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = loanRepaymentScheduleTransactionProcessorFactory
                .determineProcessor(loan.transactionProcessingStrategy());

        if (!loan.repaymentScheduleDetail().isInterestRecalculationEnabled() || loan.isNpa() || !loan.status().isActive()
                || !loanRepaymentScheduleTransactionProcessor.isInterestFirstRepaymentScheduleTransactionProcessor()) { return; }

        if (loan.loanProduct().isMultiDisburseLoan()) {
            BigDecimal disbursedAmount = loan.getDisbursedAmount();
            BigDecimal principalRepaid = loan.getLoanSummary().getTotalPrincipalRepaid();
            BigDecimal principalWrittenOff = loan.getLoanSummary().getTotalPrincipalWrittenOff();
            if (disbursedAmount.subtract(principalWrittenOff).subtract(principalRepaid).compareTo(BigDecimal.ZERO) != 1) { return; }
        }
        MonetaryCurrency currency = loan.getCurrency();
        Money totalPrincipal = Money.zero(currency);
        final List<LoanSchedulePeriodData> futureInstallments = new ArrayList<>();
        for (final LoanRepaymentScheduleInstallment currentInstallment : loan.fetchRepaymentScheduleInstallments()) {
            if (currentInstallment.isNotFullyPaidOff()) {
                if (!currentInstallment.getDueDate().isAfter(today)) {
                    totalPrincipal = totalPrincipal.plus(currentInstallment.getPrincipalOutstanding(currency));
                }
            }
        }
        LoanApplicationTerms loanApplicationTerms = constructLoanApplicationTerms(loan);
        LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = this.loanScheduleAssembler.calculatePrepaymentAmount(currency,
                today, loanApplicationTerms, loan.charges(), loan.getOfficeId(),
                loan.retreiveListOfTransactionsPostDisbursementExcludeAccruals(), loanRepaymentScheduleTransactionProcessor,
                loan.fetchRepaymentScheduleInstallments());
        Money totalAmount = totalPrincipal.plus(loanRepaymentScheduleInstallment.getFeeChargesOutstanding(currency)).plus(
                loanRepaymentScheduleInstallment.getPenaltyChargesOutstanding(currency));
        Money interestDue = Money.zero(currency);
        if (loanRepaymentScheduleInstallment.isInterestDue(currency)) {
            interestDue = loanRepaymentScheduleInstallment.getInterestOutstanding(currency);
            totalAmount = totalAmount.plus(interestDue);
        }
        boolean isNewPaymentRequired = loanRepaymentScheduleInstallment.isInterestDue(currency) || totalPrincipal.isGreaterThanZero();
        final List<LoanTransaction> modifiedTransactions = new ArrayList<>();
        List<LoanTransaction> transactions = loan.retreiveListOfTransactionsPostDisbursementExcludeAccruals();
        for (LoanTransaction loanTransaction : transactions) {
            modifiedTransactions.add(LoanTransaction.copyTransactionProperties(loanTransaction));
        }
        if (isNewPaymentRequired) {
            LoanTransaction ondayPaymentTransaction = LoanTransaction.repayment(null, totalAmount, null, today, null,
                    DateUtils.getLocalDateTimeOfTenant(), null);
            modifiedTransactions.add(ondayPaymentTransaction);
        }

        LoanScheduleModel model = this.loanScheduleAssembler.assembleForInterestRecalculation(loanApplicationTerms, loan.getOfficeId(),
                modifiedTransactions, loan.charges(), loanRepaymentScheduleTransactionProcessor, loan.fetchRepaymentScheduleInstallments(),
                loan.fetchInterestRecalculateFromDate());
        LoanScheduleData scheduleDate = model.toData();
        Collection<LoanSchedulePeriodData> periodDatas = scheduleDate.getPeriods();
        for (LoanSchedulePeriodData periodData : periodDatas) {
            if ((periodData.periodDueDate().isEqual(today) || periodData.periodDueDate().isAfter(today)) && isNewPaymentRequired) {
                LoanSchedulePeriodData loanSchedulePeriodData = LoanSchedulePeriodData.repaymentOnlyPeriod(periodData.periodNumber(),
                        periodData.periodFromDate(), periodData.periodDueDate(), totalPrincipal.getAmount(), periodData
                                .principalLoanBalanceOutstanding(), interestDue.getAmount(), loanRepaymentScheduleInstallment
                                .getFeeChargesCharged(currency).getAmount(),
                        loanRepaymentScheduleInstallment.getPenaltyChargesCharged(currency).getAmount(), totalAmount.getAmount(),
                        totalPrincipal.plus(interestDue).getAmount());
                futureInstallments.add(loanSchedulePeriodData);
                isNewPaymentRequired = false;
            } else if (periodData.periodDueDate().isAfter(today)) {
                futureInstallments.add(periodData);
            }

        }
        loanScheduleData.updateFuturePeriods(futureInstallments);
    }

    @Override
    public LoanScheduleData generateLoanScheduleForVariableInstallmentRequest(Long loanId, final String json) {
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        this.loanScheduleAssembler.assempleVariableScheduleFrom(loan, json);
        return constructLoanScheduleData(loan);
    }

    private LoanScheduleData constructLoanScheduleData(Loan loan) {
        Collection<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        final List<LoanSchedulePeriodData> installmentData = new ArrayList<>();
        final MonetaryCurrency currency = loan.getCurrency();
        Money outstanding = loan.getPrincpal();

        Set<LoanDisbursementDetails> disbursementDetails = new HashSet<>();
        if (loan.isMultiDisburmentLoan()) {
            disbursementDetails = loan.getDisbursementDetails();
            outstanding = outstanding.zero();
        }
        Money principal = outstanding;
        Iterator<LoanDisbursementDetails> disbursementItr = disbursementDetails.iterator();
        LoanDisbursementDetails loanDisbursementDetails = null;
        if (disbursementItr.hasNext()) {
            loanDisbursementDetails = disbursementItr.next();
        }

        Money totalInterest = principal.zero();
        Money totalCharge = principal.zero();
        Money totalPenalty = principal.zero();

        for (LoanRepaymentScheduleInstallment installment : installments) {
            if (loanDisbursementDetails != null
                    && !loanDisbursementDetails.expectedDisbursementDateAsLocalDate().isAfter(installment.getDueDate())) {
                outstanding = outstanding.plus(loanDisbursementDetails.principal());
                principal = principal.plus(loanDisbursementDetails.principal());
                if (disbursementItr.hasNext()) {
                    loanDisbursementDetails = disbursementItr.next();
                } else {
                    loanDisbursementDetails = null;
                }
            }
            outstanding = outstanding.minus(installment.getPrincipal(currency));
            LoanSchedulePeriodData loanSchedulePeriodData = LoanSchedulePeriodData.repaymentOnlyPeriod(installment.getInstallmentNumber(),
                    installment.getFromDate(), installment.getDueDate(), installment.getPrincipal(currency).getAmount(),
                    outstanding.getAmount(), installment.getInterestCharged(currency).getAmount(),
                    installment.getFeeChargesCharged(currency).getAmount(), installment.getPenaltyChargesCharged(currency).getAmount(),
                    installment.getDue(currency).getAmount(), installment.getTotalPrincipalAndInterest(currency).getAmount());
            installmentData.add(loanSchedulePeriodData);
            totalInterest = totalInterest.plus(installment.getInterestCharged(currency));
            totalCharge = totalCharge.plus(installment.getFeeChargesCharged(currency));
            totalPenalty = totalPenalty.plus(installment.getPenaltyChargesCharged(currency));
        }

        CurrencyData currencyData = this.currencyReadPlatformService.retrieveCurrency(currency.getCode());

        LoanScheduleData scheduleData = new LoanScheduleData(currencyData, installmentData, loan.getLoanRepaymentScheduleDetail()
                .getNumberOfRepayments(), principal.getAmount(), principal.getAmount(), totalInterest.getAmount(), totalCharge.getAmount(),
                totalPenalty.getAmount(), principal.plus(totalCharge).plus(totalInterest).plus(totalPenalty).getAmount());

        return scheduleData;
    }

    private LoanApplicationTerms constructLoanApplicationTerms(final Loan loan) {
        final LocalDate recalculateFrom = null;
        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
        LoanApplicationTerms loanApplicationTerms = loan.constructLoanApplicationTerms(scheduleGeneratorDTO);
        return loanApplicationTerms;
    }

}