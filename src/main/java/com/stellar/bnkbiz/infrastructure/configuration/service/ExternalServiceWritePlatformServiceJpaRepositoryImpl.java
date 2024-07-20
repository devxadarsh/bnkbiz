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
package com.stellar.bnkbiz.infrastructure.configuration.service;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.stellar.bnkbiz.infrastructure.configuration.data.ExternalServicesData;
import com.stellar.bnkbiz.infrastructure.configuration.domain.ExternalServicesProperties;
import com.stellar.bnkbiz.infrastructure.configuration.domain.ExternalServicesPropertiesRepository;
import com.stellar.bnkbiz.infrastructure.configuration.domain.ExternalServicesPropertiesRepositoryWrapper;
import com.stellar.bnkbiz.infrastructure.configuration.serialization.ExternalServicesPropertiesCommandFromApiJsonDeserializer;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResultBuilder;
import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExternalServiceWritePlatformServiceJpaRepositoryImpl implements ExternalServiceWritePlatformService {

    @SuppressWarnings("unused")
    private final static Logger logger = LoggerFactory.getLogger(ExternalServiceWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final ExternalServicesPropertiesRepositoryWrapper repositoryWrapper;
    private final ExternalServicesPropertiesRepository repository;
    private final ExternalServicesPropertiesCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final ExternalServicesReadPlatformService readPlatformService;

    @Autowired
    public ExternalServiceWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final ExternalServicesPropertiesRepositoryWrapper repositoryWrapper, final ExternalServicesPropertiesRepository repository,
            final ExternalServicesPropertiesCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final ExternalServicesReadPlatformService readPlatformService) {

        this.context = context;
        this.repositoryWrapper = repositoryWrapper;
        this.repository = repository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.readPlatformService = readPlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult updateExternalServicesProperties(String externalServiceName, JsonCommand command) {
        // TODO Auto-generated method stub
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForUpdate(command.json(), externalServiceName);
        Set<String> keyName = this.fromApiJsonDeserializer.getNameKeys(command.json());
        ExternalServicesData external = this.readPlatformService.getExternalServiceDetailsByServiceName(externalServiceName);
        Long externalServiceId = external.getId();
        Iterator<String> it = keyName.iterator();
        Map<String, Object> changesList = new LinkedHashMap<>();
        while (it.hasNext()) {
            String name = it.next();
            final ExternalServicesProperties externalServicesProperties = this.repositoryWrapper.findOneByIdAndName(externalServiceId, name,
                    externalServiceName);
            final Map<String, Object> changes = externalServicesProperties.update(command, name);
            changesList.putAll(changes);
            if (!changes.isEmpty()) {
                this.repository.saveAndFlush(externalServicesProperties);
            }
        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()).withEntityId(externalServiceId).with(changesList).build();
        //

    }

}
