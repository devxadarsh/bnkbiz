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
package com.stellar.bnkbiz.mix.service;

import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResultBuilder;
import com.stellar.bnkbiz.mix.domain.MixTaxonomyMapping;
import com.stellar.bnkbiz.mix.domain.MixTaxonomyMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MixTaxonomyMappingWritePlatformServiceImpl implements MixTaxonomyMappingWritePlatformService {

    private final MixTaxonomyMappingRepository mappingRepository;

    @Autowired
    public MixTaxonomyMappingWritePlatformServiceImpl(final MixTaxonomyMappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult updateMapping(final Long mappingId, final JsonCommand command) {
        try {
            MixTaxonomyMapping mapping = this.mappingRepository.findOne(mappingId);
            if (mapping == null) {
                mapping = MixTaxonomyMapping.fromJson(command);
            } else {
                mapping.update(command);
            }

            this.mappingRepository.saveAndFlush(mapping);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(mapping.getId()).build();

        } catch (final DataIntegrityViolationException dve) {
            return CommandProcessingResult.empty();
        }
    }
}