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
package com.stellar.bnkbiz.spm.service;

import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
import com.stellar.bnkbiz.spm.domain.LookupTable;
import com.stellar.bnkbiz.spm.domain.Survey;
import com.stellar.bnkbiz.spm.repository.LookupTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LookupTableService {

    private final PlatformSecurityContext securityContext;
    private final LookupTableRepository lookupTableRepository;

    @Autowired
    public LookupTableService(final PlatformSecurityContext securityContext,
                              final LookupTableRepository lookupTableRepository) {
        super();
        this.securityContext = securityContext;
        this.lookupTableRepository = lookupTableRepository;
    }

    public List<LookupTable> findByKey(final String key) {
        this.securityContext.authenticatedUser();

        return this.lookupTableRepository.findByKey(key);
    }

    public List<LookupTable> findBySurvey(final Survey survey) {
        this.securityContext.authenticatedUser();

        return this.lookupTableRepository.findBySurvey(survey);
    }

    public List<LookupTable> createLookupTable(final List<LookupTable> lookupTable) {
        this.securityContext.authenticatedUser();

        return this.lookupTableRepository.save(lookupTable);
    }
}
