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
package com.stellar.bnkbiz.portfolio.client.service;

import java.util.Map;
import java.util.Set;

import com.stellar.bnkbiz.accounting.journalentry.service.JournalEntryWritePlatformService;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResultBuilder;
import com.stellar.bnkbiz.organisation.office.domain.OrganisationCurrencyRepositoryWrapper;
import com.stellar.bnkbiz.portfolio.client.domain.Client;
import com.stellar.bnkbiz.portfolio.client.domain.ClientCharge;
import com.stellar.bnkbiz.portfolio.client.domain.ClientChargePaidBy;
import com.stellar.bnkbiz.portfolio.client.domain.ClientRepositoryWrapper;
import com.stellar.bnkbiz.portfolio.client.domain.ClientTransaction;
import com.stellar.bnkbiz.portfolio.client.domain.ClientTransactionRepositoryWrapper;
import com.stellar.bnkbiz.portfolio.client.exception.ClientTransactionCannotBeUndoneException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientTransactionWritePlatformServiceJpaRepositoryImpl implements ClientTransactionWritePlatformService {

    private final ClientTransactionRepositoryWrapper clientTransactionRepository;

    private final ClientRepositoryWrapper clientRepository;
    private final OrganisationCurrencyRepositoryWrapper organisationCurrencyRepository;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;

    @Autowired
    public ClientTransactionWritePlatformServiceJpaRepositoryImpl(final ClientTransactionRepositoryWrapper clientTransactionRepository,
            final ClientRepositoryWrapper clientRepositoryWrapper,
            final OrganisationCurrencyRepositoryWrapper organisationCurrencyRepositoryWrapper,
            JournalEntryWritePlatformService journalEntryWritePlatformService) {
        this.clientTransactionRepository = clientTransactionRepository;
        this.clientRepository = clientRepositoryWrapper;
        this.organisationCurrencyRepository = organisationCurrencyRepositoryWrapper;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
    }

    @Override
    public CommandProcessingResult undo(Long clientId, Long transactionId) {

        final Client client = this.clientRepository.getActiveClientInUserScope(clientId);

        final ClientTransaction clientTransaction = this.clientTransactionRepository.findOneWithNotFoundDetection(clientId, transactionId);

        // validate that transaction can be undone
        if (clientTransaction.isReversed()) { throw new ClientTransactionCannotBeUndoneException(clientId, transactionId); }

        // mark transaction as reversed
        clientTransaction.reverse();

        // revert any charges paid back to their original state
        if (clientTransaction.isPayChargeTransaction() || clientTransaction.isWaiveChargeTransaction()) {
            // undo charge
            final Set<ClientChargePaidBy> chargesPaidBy = clientTransaction.getClientChargePaidByCollection();
            for (final ClientChargePaidBy clientChargePaidBy : chargesPaidBy) {
                final ClientCharge clientCharge = clientChargePaidBy.getClientCharge();
                clientCharge.setCurrency(
                        organisationCurrencyRepository.findOneWithNotFoundDetection(clientCharge.getCharge().getCurrencyCode()));
                if (clientTransaction.isPayChargeTransaction()) {
                    clientCharge.undoPayment(clientTransaction.getAmount());
                } else if (clientTransaction.isWaiveChargeTransaction()) {
                    clientCharge.undoWaiver(clientTransaction.getAmount());
                }
            }
        }

        // generate accounting entries
        this.clientTransactionRepository.saveAndFlush(clientTransaction);
        generateAccountingEntries(clientTransaction);

        return new CommandProcessingResultBuilder() //
                .withEntityId(transactionId) //
                .withOfficeId(client.officeId()) //
                .withClientId(clientId) //
                .build();
    }

    private void generateAccountingEntries(ClientTransaction clientTransaction) {
        Map<String, Object> accountingBridgeData = clientTransaction.toMapData();
        journalEntryWritePlatformService.createJournalEntriesForClientTransactions(accountingBridgeData);
    }

}
