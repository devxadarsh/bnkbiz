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
package com.stellar.bnkbiz.infrastructure.codes.service;

import java.util.Map;

import com.stellar.bnkbiz.infrastructure.codes.domain.Code;
import com.stellar.bnkbiz.infrastructure.codes.domain.CodeRepository;
import com.stellar.bnkbiz.infrastructure.codes.exception.CodeNotFoundException;
import com.stellar.bnkbiz.infrastructure.codes.exception.SystemDefinedCodeCannotBeChangedException;
import com.stellar.bnkbiz.infrastructure.codes.serialization.CodeCommandFromApiJsonDeserializer;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResultBuilder;
import com.stellar.bnkbiz.infrastructure.core.exception.PlatformDataIntegrityException;
import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeWritePlatformServiceJpaRepositoryImpl implements CodeWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(CodeWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final CodeRepository codeRepository;
    private final CodeCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public CodeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final CodeRepository codeRepository,
            final CodeCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.context = context;
        this.codeRepository = codeRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Transactional
    @Override
    @CacheEvict(value = "codes", key = "T(com.stellar.bnkbiz.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('cv')")
    public CommandProcessingResult createCode(final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Code code = Code.fromJson(command);
            this.codeRepository.save(code);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(code.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleCodeDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "codes", key = "T(com.stellar.bnkbiz.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('cv')")
    public CommandProcessingResult updateCode(final Long codeId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Code code = retrieveCodeBy(codeId);
            final Map<String, Object> changes = code.update(command);

            if (!changes.isEmpty()) {
                this.codeRepository.save(code);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(codeId) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleCodeDataIntegrityIssues(command, dve);
            return null;
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "codes", key = "T(com.stellar.bnkbiz.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('cv')")
    public CommandProcessingResult deleteCode(final Long codeId) {

        this.context.authenticatedUser();

        final Code code = retrieveCodeBy(codeId);
        if (code.isSystemDefined()) { throw new SystemDefinedCodeCannotBeChangedException(); }

        try {
            this.codeRepository.delete(code);
            this.codeRepository.flush();
        } catch (final DataIntegrityViolationException e) {
            throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: " + e.getMostSpecificCause());
        }
        return new CommandProcessingResultBuilder().withEntityId(codeId).build();
    }

    private Code retrieveCodeBy(final Long codeId) {
        final Code code = this.codeRepository.findOne(codeId);
        if (code == null) { throw new CodeNotFoundException(codeId.toString()); }
        return code;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleCodeDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("code_name")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.code.duplicate.name", "A code with name '" + name + "' already exists",
                    "name", name);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}