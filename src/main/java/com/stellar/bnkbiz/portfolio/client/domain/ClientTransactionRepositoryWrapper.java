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
package com.stellar.bnkbiz.portfolio.client.domain;

import com.stellar.bnkbiz.organisation.office.domain.OrganisationCurrencyRepositoryWrapper;
import com.stellar.bnkbiz.portfolio.client.exception.ClientTransactionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ClientTransactionRepositoryWrapper {

    private final ClientTransactionRepository repository;
    private final OrganisationCurrencyRepositoryWrapper organisationCurrencyRepository;

    @Autowired
    public ClientTransactionRepositoryWrapper(final ClientTransactionRepository repository,
            final OrganisationCurrencyRepositoryWrapper currencyRepositoryWrapper) {
        this.repository = repository;
        this.organisationCurrencyRepository = currencyRepositoryWrapper;
    }

    public ClientTransaction findOneWithNotFoundDetection(final Long clientId, final Long transactionId) {
        final ClientTransaction clientTransaction = this.repository.findOne(transactionId);
        if (clientTransaction == null
                || clientTransaction.getClientId() != clientId) { throw new ClientTransactionNotFoundException(clientId, transactionId); }
        // enrich Client charge with details of Organizational currency
        clientTransaction.setCurrency(organisationCurrencyRepository.findOneWithNotFoundDetection(clientTransaction.getCurrencyCode()));
        return clientTransaction;
    }

    public void save(final ClientTransaction clientTransaction) {
        this.repository.save(clientTransaction);
    }

    public void saveAndFlush(final ClientTransaction clientTransaction) {
        this.repository.saveAndFlush(clientTransaction);
    }

    public void delete(final ClientTransaction clientTransaction) {
        this.repository.delete(clientTransaction);
    }

}
