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
package com.stellar.bnkbiz.portfolio.loanaccount.domain;

import com.stellar.bnkbiz.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.transactionprocessor.impl.CreocoreLoanRepaymentScheduleTransactionProcessor;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.transactionprocessor.impl.EarlyPaymentLoanRepaymentScheduleTransactionProcessor;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.transactionprocessor.impl.HeavensFamilyLoanRepaymentScheduleTransactionProcessor;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.transactionprocessor.impl.InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.transactionprocessor.impl.FineractStyleLoanRepaymentScheduleTransactionProcessor;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.transactionprocessor.impl.PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.transactionprocessor.impl.RBILoanRepaymentScheduleTransactionProcessor;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.springframework.stereotype.Component;

@Component
public class LoanRepaymentScheduleTransactionProcessorFactory {

    public LoanRepaymentScheduleTransactionProcessor determineProcessor(
            final LoanTransactionProcessingStrategy transactionProcessingStrategy) {

        LoanRepaymentScheduleTransactionProcessor processor = new PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor();

        if (transactionProcessingStrategy != null) {

            if (transactionProcessingStrategy.isStandardStrategy()) {
                processor = new FineractStyleLoanRepaymentScheduleTransactionProcessor();
            }

            if (transactionProcessingStrategy.isHeavensfamilyStrategy()) {
                processor = new HeavensFamilyLoanRepaymentScheduleTransactionProcessor();
            }

            if (transactionProcessingStrategy.isEarlyPaymentStrategy()) {
                processor = new EarlyPaymentLoanRepaymentScheduleTransactionProcessor();
            }

            if (transactionProcessingStrategy.isCreocoreStrategy()) {
                processor = new CreocoreLoanRepaymentScheduleTransactionProcessor();
            }

            if (transactionProcessingStrategy.isIndianRBIStrategy()) {
                processor = new RBILoanRepaymentScheduleTransactionProcessor();
            }

            if (transactionProcessingStrategy.isPrincipalInterestPenaltiesFeesOrderStrategy()) {
                processor = new PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor();
            }

            if (transactionProcessingStrategy.isInterestPrincipalPenaltiesFeesOrderStrategy()) {
                processor = new InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor();
            }
        }

        return processor;
    }
}