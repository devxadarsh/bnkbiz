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
package com.stellar.bnkbiz.portfolio.collectionsheet.data;

import java.util.Collection;

import com.stellar.bnkbiz.portfolio.paymenttype.data.PaymentTypeData;
import java.time.LocalDate;

/**
 * Immutable data object for collection sheet.
 */
public class IndividualCollectionSheetData {

    @SuppressWarnings("unused")
    private final LocalDate dueDate;
    @SuppressWarnings("unused")
    private final Collection<IndividualClientData> clients;

    @SuppressWarnings("unused")
    private final Collection<PaymentTypeData> paymentTypeOptions;

    public static IndividualCollectionSheetData instance(final LocalDate date, final Collection<IndividualClientData> clients,
            final Collection<PaymentTypeData> paymentTypeOptions) {
        return new IndividualCollectionSheetData(date, clients, paymentTypeOptions);
    }

    private IndividualCollectionSheetData(final LocalDate dueDate, final Collection<IndividualClientData> clients,
            final Collection<PaymentTypeData> paymentTypeOptions) {
        this.dueDate = dueDate;
        this.clients = clients;
        this.paymentTypeOptions = paymentTypeOptions;
    }

}