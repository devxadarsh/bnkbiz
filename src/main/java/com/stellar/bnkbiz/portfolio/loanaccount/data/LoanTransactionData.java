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
package com.stellar.bnkbiz.portfolio.loanaccount.data;

import java.math.BigDecimal;
import java.util.Collection;

import com.stellar.bnkbiz.organisation.monetary.data.CurrencyData;
import com.stellar.bnkbiz.portfolio.account.data.AccountTransferData;
import com.stellar.bnkbiz.portfolio.paymentdetail.data.PaymentDetailData;
import com.stellar.bnkbiz.portfolio.paymenttype.data.PaymentTypeData;
import java.time.LocalDate;

/**
 * Immutable data object representing a loan transaction.
 */
public class LoanTransactionData {

    private final Long id;
    private final Long officeId;
    private final String officeName;

    private final LoanTransactionEnumData type;

    private final LocalDate date;

    private final CurrencyData currency;
    private final PaymentDetailData paymentDetailData;

    private final BigDecimal amount;
    private final BigDecimal principalPortion;
    private final BigDecimal interestPortion;
    private final BigDecimal feeChargesPortion;
    private final BigDecimal penaltyChargesPortion;
    private final BigDecimal overpaymentPortion;
    private final BigDecimal unrecognizedIncomePortion;
    private final String externalId;
    private final AccountTransferData transfer;
    private final BigDecimal fixedEmiAmount;
    private final BigDecimal outstandingLoanBalance;
    @SuppressWarnings("unused")
    private final LocalDate submittedOnDate;
    private final boolean manuallyReversed;
    @SuppressWarnings("unused")
	private final LocalDate possibleNextRepaymentDate;

    // templates
    final Collection<PaymentTypeData> paymentTypeOptions;

    public static LoanTransactionData templateOnTop(final LoanTransactionData loanTransactionData,
            final Collection<PaymentTypeData> paymentTypeOptions) {
        return new LoanTransactionData(loanTransactionData.id, loanTransactionData.officeId, loanTransactionData.officeName,
                loanTransactionData.type, loanTransactionData.paymentDetailData, loanTransactionData.currency, loanTransactionData.date,
                loanTransactionData.amount, loanTransactionData.principalPortion, loanTransactionData.interestPortion,
                loanTransactionData.feeChargesPortion, loanTransactionData.penaltyChargesPortion, loanTransactionData.overpaymentPortion,
                loanTransactionData.unrecognizedIncomePortion, paymentTypeOptions, loanTransactionData.externalId,
                loanTransactionData.transfer, loanTransactionData.fixedEmiAmount, loanTransactionData.outstandingLoanBalance,
                loanTransactionData.manuallyReversed);

    }

    public LoanTransactionData(final Long id, final Long officeId, final String officeName, final LoanTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final CurrencyData currency, final LocalDate date, final BigDecimal amount,
            final BigDecimal principalPortion, final BigDecimal interestPortion, final BigDecimal feeChargesPortion,
            final BigDecimal penaltyChargesPortion, final BigDecimal overpaymentPortion, final String externalId,
            final AccountTransferData transfer, BigDecimal fixedEmiAmount, BigDecimal outstandingLoanBalance,
            final BigDecimal unrecognizedIncomePortion,final boolean manuallyReversed) {
        this(id, officeId, officeName, transactionType, paymentDetailData, currency, date, amount, principalPortion, interestPortion,
                feeChargesPortion, penaltyChargesPortion, overpaymentPortion, unrecognizedIncomePortion, null, externalId, transfer,
                fixedEmiAmount, outstandingLoanBalance,manuallyReversed);
    }
 
    public LoanTransactionData(final Long id, final Long officeId, final String officeName, final LoanTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final CurrencyData currency, final LocalDate date, final BigDecimal amount,
            final BigDecimal principalPortion, final BigDecimal interestPortion, final BigDecimal feeChargesPortion,
            final BigDecimal penaltyChargesPortion, final BigDecimal overpaymentPortion, BigDecimal unrecognizedIncomePortion,
            final Collection<PaymentTypeData> paymentTypeOptions, final String externalId, final AccountTransferData transfer,
            final BigDecimal fixedEmiAmount, BigDecimal outstandingLoanBalance,boolean manuallyReversed) {
        this(id, officeId, officeName, transactionType, paymentDetailData, currency, date, amount, principalPortion, interestPortion,
                feeChargesPortion, penaltyChargesPortion, overpaymentPortion, unrecognizedIncomePortion, paymentTypeOptions, externalId,
                transfer, fixedEmiAmount, outstandingLoanBalance, null,manuallyReversed);
    }

    public LoanTransactionData(final Long id, final Long officeId, final String officeName, final LoanTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final CurrencyData currency, final LocalDate date, final BigDecimal amount,
            final BigDecimal principalPortion, final BigDecimal interestPortion, final BigDecimal feeChargesPortion,
            final BigDecimal penaltyChargesPortion, final BigDecimal overpaymentPortion, final BigDecimal unrecognizedIncomePortion,
            final String externalId, final AccountTransferData transfer, BigDecimal fixedEmiAmount, BigDecimal outstandingLoanBalance,
            LocalDate submittedOnDate,final boolean manuallyReversed) {
        this(id, officeId, officeName, transactionType, paymentDetailData, currency, date, amount, principalPortion, interestPortion,
                feeChargesPortion, penaltyChargesPortion, overpaymentPortion, unrecognizedIncomePortion, null, externalId, transfer,
                fixedEmiAmount, outstandingLoanBalance, submittedOnDate,manuallyReversed);
    }

    public LoanTransactionData(final Long id, final Long officeId, final String officeName, final LoanTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final CurrencyData currency, final LocalDate date, final BigDecimal amount,
            final BigDecimal principalPortion, final BigDecimal interestPortion, final BigDecimal feeChargesPortion,
            final BigDecimal penaltyChargesPortion, final BigDecimal overpaymentPortion, final BigDecimal unrecognizedIncomePortion,
            final Collection<PaymentTypeData> paymentTypeOptions, final String externalId, final AccountTransferData transfer,
            final BigDecimal fixedEmiAmount, BigDecimal outstandingLoanBalance, final LocalDate submittedOnDate,final boolean manuallyReversed) {
        this.id = id;
        this.officeId = officeId;
        this.officeName = officeName;
        this.type = transactionType;
        this.paymentDetailData = paymentDetailData;
        this.currency = currency;
        this.date = date;
        this.amount = amount;
        this.principalPortion = principalPortion;
        this.interestPortion = interestPortion;
        this.feeChargesPortion = feeChargesPortion;
        this.penaltyChargesPortion = penaltyChargesPortion;
        this.unrecognizedIncomePortion = unrecognizedIncomePortion;
        this.paymentTypeOptions = paymentTypeOptions;
        this.externalId = externalId;
        this.transfer = transfer;
        this.overpaymentPortion = overpaymentPortion;
        this.fixedEmiAmount = fixedEmiAmount;
        this.outstandingLoanBalance = outstandingLoanBalance;
        this.submittedOnDate = submittedOnDate;
        this.manuallyReversed = manuallyReversed;
        this.possibleNextRepaymentDate = null;
    }

    public LoanTransactionData(Long id, LoanTransactionEnumData transactionType, LocalDate date, BigDecimal totalAmount,
            BigDecimal principalPortion, BigDecimal interestPortion, BigDecimal feeChargesPortion, BigDecimal penaltyChargesPortion,
            BigDecimal overPaymentPortion, BigDecimal unrecognizedIncomePortion, BigDecimal outstandingLoanBalance,final boolean manuallyReversed) {
        this(id, null, null, transactionType, null, null, date, totalAmount, principalPortion, interestPortion, feeChargesPortion,
                penaltyChargesPortion, overPaymentPortion, unrecognizedIncomePortion, null, null, null, null, outstandingLoanBalance, null,
                manuallyReversed);
    }
    
    public static LoanTransactionData LoanTransactionDataForDisbursalTemplate(final LoanTransactionEnumData transactionType, final LocalDate expectedDisbursedOnLocalDateForTemplate, 
			final BigDecimal disburseAmountForTemplate,	final Collection<PaymentTypeData> paymentOptions,
			final BigDecimal retriveLastEmiAmount, final LocalDate possibleNextRepaymentDate) {
		    final Long id = null;
		    final Long officeId = null;
		    final String officeName = null;
		    final PaymentDetailData paymentDetailData = null;
		    final CurrencyData currency = null;
		    final BigDecimal unrecognizedIncomePortion = null;
		    final BigDecimal principalPortion = null;;
		    final BigDecimal interestPortion = null;
		    final BigDecimal feeChargesPortion = null;
		    final BigDecimal penaltyChargesPortion = null;
		    final BigDecimal overpaymentPortion = null;
		    final String externalId = null;
		    final BigDecimal outstandingLoanBalance = null;
		    final AccountTransferData transfer = null;
		    final LocalDate submittedOnDate = null;
		    final boolean manuallyReversed = false;
			return new LoanTransactionData(id, officeId, officeName, transactionType, paymentDetailData, currency, expectedDisbursedOnLocalDateForTemplate,
					disburseAmountForTemplate, principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion, overpaymentPortion,	unrecognizedIncomePortion, 
					paymentOptions, transfer, externalId, retriveLastEmiAmount, outstandingLoanBalance, submittedOnDate, manuallyReversed, possibleNextRepaymentDate);
		
	}

    private LoanTransactionData(Long id , final Long officeId, final String officeName, LoanTransactionEnumData transactionType, final PaymentDetailData paymentDetailData,
    		final CurrencyData currency, final LocalDate date,	BigDecimal amount, final BigDecimal principalPortion, final BigDecimal interestPortion, 
    		final BigDecimal feeChargesPortion, final BigDecimal penaltyChargesPortion, final BigDecimal overpaymentPortion, BigDecimal unrecognizedIncomePortion,	Collection<PaymentTypeData> paymentOptions,
    		final AccountTransferData transfer, final String externalId, final BigDecimal fixedEmiAmount, BigDecimal outstandingLoanBalance, 
    		final LocalDate submittedOnDate, final boolean manuallyReversed, final LocalDate possibleNextRepaymentDate) {
    	 this.id = id;
         this.officeId = officeId;
         this.officeName = officeName;
         this.type = transactionType;
         this.paymentDetailData = paymentDetailData;
         this.currency = currency;
         this.date = date;
         this.amount = amount;
         this.principalPortion = principalPortion;
         this.interestPortion = interestPortion;
         this.feeChargesPortion = feeChargesPortion;
         this.penaltyChargesPortion = penaltyChargesPortion;
         this.unrecognizedIncomePortion = unrecognizedIncomePortion;
         this.paymentTypeOptions = paymentOptions;
         this.externalId = externalId;
         this.transfer = transfer;
         this.overpaymentPortion = overpaymentPortion;
         this.fixedEmiAmount = fixedEmiAmount;
         this.outstandingLoanBalance = outstandingLoanBalance;
         this.submittedOnDate = submittedOnDate;
         this.manuallyReversed = manuallyReversed;
         this.possibleNextRepaymentDate = possibleNextRepaymentDate;
	}

	

	public LocalDate dateOf() {
        return this.date;
    }

    public boolean isNotDisbursement() {
        return Integer.valueOf(1).equals(this.type.id());
    }

    
    public BigDecimal getAmount() {
        return this.amount;
    }

    
    public BigDecimal getUnrecognizedIncomePortion() {
        return this.unrecognizedIncomePortion;
    }

    
    public BigDecimal getInterestPortion() {
        return this.interestPortion;
    }
}