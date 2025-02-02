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
package com.stellar.bnkbiz.commands.service;

import java.util.Collection;

import com.stellar.bnkbiz.commands.data.AuditData;
import com.stellar.bnkbiz.commands.data.AuditSearchData;
import com.stellar.bnkbiz.infrastructure.core.data.PaginationParameters;
import com.stellar.bnkbiz.infrastructure.core.service.Page;

public interface AuditReadPlatformService {

    Collection<AuditData> retrieveAuditEntries(String extraCriteria, boolean includeJson);

    Page<AuditData> retrievePaginatedAuditEntries(String extraCriteria, boolean includeJson, PaginationParameters parameters);

    Collection<AuditData> retrieveAllEntriesToBeChecked(String extraCriteria, boolean includeJson);

    AuditData retrieveAuditEntry(Long auditId);

    AuditSearchData retrieveSearchTemplate(String useType);
}