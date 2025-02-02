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
package com.stellar.bnkbiz.portfolio.savings.service;

import java.util.Collection;
import java.util.Map;

import com.stellar.bnkbiz.infrastructure.core.data.PaginationParameters;
import com.stellar.bnkbiz.infrastructure.core.service.Page;
import com.stellar.bnkbiz.portfolio.account.data.AccountTransferDTO;
import com.stellar.bnkbiz.portfolio.savings.DepositAccountType;
import com.stellar.bnkbiz.portfolio.savings.data.DepositAccountData;
import com.stellar.bnkbiz.portfolio.savings.data.SavingsAccountTransactionData;

public interface DepositAccountReadPlatformService {

    Collection<DepositAccountData> retrieveAll(final DepositAccountType depositAccountType, final PaginationParameters paginationParameters);

    Page<DepositAccountData> retrieveAllPaged(final DepositAccountType depositAccountType, final PaginationParameters paginationParameters);

    Collection<DepositAccountData> retrieveAllForLookup(final DepositAccountType depositAccountType);

    DepositAccountData retrieveOne(final DepositAccountType depositAccountType, Long accountId);

    DepositAccountData retrieveOneWithClosureTemplate(final DepositAccountType depositAccountType, Long accountId);

    DepositAccountData retrieveOneWithChartSlabs(final DepositAccountType depositAccountType, Long productId);

    Collection<SavingsAccountTransactionData> retrieveAllTransactions(final DepositAccountType depositAccountType, Long accountId);

    DepositAccountData retrieveTemplate(final DepositAccountType depositAccountType, Long clientId, Long groupId, Long productId,
            boolean staffInSelectedOfficeOnly);

    Collection<DepositAccountData> retrieveForMaturityUpdate();

    SavingsAccountTransactionData retrieveRecurringAccountDepositTransactionTemplate(final Long accountId);

    Collection<AccountTransferDTO> retrieveDataForInterestTransfer();

    Collection<Map<String, Object>> retriveDataForRDScheduleCreation();
}
