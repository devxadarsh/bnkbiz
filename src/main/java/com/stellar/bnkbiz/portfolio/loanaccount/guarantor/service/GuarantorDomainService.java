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
package com.stellar.bnkbiz.portfolio.loanaccount.guarantor.service;

import com.stellar.bnkbiz.portfolio.loanaccount.domain.Loan;
import com.stellar.bnkbiz.portfolio.loanaccount.guarantor.domain.GuarantorFundingDetails;
import java.time.LocalDate;

public interface GuarantorDomainService {

    void releaseGuarantor(GuarantorFundingDetails guarantorFundingDetails, LocalDate transactionDate);

    void validateGuarantorBusinessRules(Loan loan);

    void assignGuarantor(GuarantorFundingDetails guarantorFundingDetails, LocalDate transactionDate);

    void transaferFundsFromGuarantor(Loan loan);

}
