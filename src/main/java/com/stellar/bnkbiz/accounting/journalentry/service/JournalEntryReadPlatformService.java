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
package com.stellar.bnkbiz.accounting.journalentry.service;

import java.util.Date;

import com.stellar.bnkbiz.accounting.journalentry.data.JournalEntryAssociationParametersData;
import com.stellar.bnkbiz.accounting.journalentry.data.JournalEntryData;
import com.stellar.bnkbiz.accounting.journalentry.data.OfficeOpeningBalancesData;
import com.stellar.bnkbiz.infrastructure.core.service.Page;
import com.stellar.bnkbiz.infrastructure.core.service.SearchParameters;

public interface JournalEntryReadPlatformService {

    JournalEntryData retrieveGLJournalEntryById(long glJournalEntryId, JournalEntryAssociationParametersData associationParametersData);

    Page<JournalEntryData> retrieveAll(SearchParameters searchParameters, Long glAccountId, Boolean onlyManualEntries, Date fromDate,
            Date toDate, String transactionId, Integer entityType, JournalEntryAssociationParametersData associationParametersData);

    OfficeOpeningBalancesData retrieveOfficeOpeningBalances(Long officeId, String currencyCode);

    Page<JournalEntryData> retrieveJournalEntriesByEntityId(String transactionId, Long entityId, Integer entityType) ;
}
