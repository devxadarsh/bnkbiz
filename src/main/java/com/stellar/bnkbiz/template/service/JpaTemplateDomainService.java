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
package com.stellar.bnkbiz.template.service;

import java.util.ArrayList;
import java.util.List;

import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResultBuilder;
import com.stellar.bnkbiz.template.domain.Template;
import com.stellar.bnkbiz.template.domain.TemplateEntity;
import com.stellar.bnkbiz.template.domain.TemplateMapper;
import com.stellar.bnkbiz.template.domain.TemplateRepository;
import com.stellar.bnkbiz.template.domain.TemplateType;
import com.stellar.bnkbiz.template.exception.TemplateNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

@Service
public class JpaTemplateDomainService implements TemplateDomainService {

    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_TEXT = "text";
    // private static final String PROPERTY_MAPPERS = "mappers";
    private static final String PROPERTY_ENTITY = "entity";
    private static final String PROPERTY_TYPE = "type";

    @Autowired
    private TemplateRepository templateRepository;

    @Override
    public List<Template> getAll() {
        return this.templateRepository.findAll();
    }

    @Override
    public Template findOneById(final Long id) {
        final Template template = this.templateRepository.findOne(id);
        if (template == null) {
            throw new TemplateNotFoundException(id);
        }
        return template;
    }

    @Transactional
    @Override
    public CommandProcessingResult createTemplate(final JsonCommand command) {
        // FIXME - no validation here of the data in the command object, is
        // name, text populated etc
        // FIXME - handle cases where data integrity constraints are fired from
        // database when saving.
        final Template template = Template.fromJson(command);

        this.templateRepository.saveAndFlush(template);
        return new CommandProcessingResultBuilder().withEntityId(
                template.getId()).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult updateTemplate(final Long templateId,
            final JsonCommand command) {
        // FIXME - no validation here of the data in the command object, is
        // name, text populated etc
        // FIXME - handle cases where data integrity constraints are fired from
        // database when saving.

        final Template template = findOneById(templateId);
        template.setName(command.stringValueOfParameterNamed(PROPERTY_NAME));
        template.setText(command.stringValueOfParameterNamed(PROPERTY_TEXT));
        template.setEntity(TemplateEntity.values()[command
                .integerValueSansLocaleOfParameterNamed(PROPERTY_ENTITY)]);
        final int templateTypeId = command
                .integerValueSansLocaleOfParameterNamed(PROPERTY_TYPE);
        TemplateType type = null;
        switch (templateTypeId) {
            case 0 :
                type = TemplateType.DOCUMENT;
                break;
            case 2 :
                type = TemplateType.SMS;
                break;
        }
        template.setType(type);

        final JsonArray array = command.arrayOfParameterNamed("mappers");
        final List<TemplateMapper> mappersList = new ArrayList<>();
        for (final JsonElement element : array) {
            mappersList.add(new TemplateMapper(element.getAsJsonObject()
                    .get("mappersorder").getAsInt(), element.getAsJsonObject()
                    .get("mapperskey").getAsString(), element.getAsJsonObject()
                    .get("mappersvalue").getAsString()));
        }
        template.setMappers(mappersList);

        this.templateRepository.saveAndFlush(template);

        return new CommandProcessingResultBuilder()
                .withCommandId(command.commandId())
                .withEntityId(template.getId()).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult removeTemplate(final Long templateId) {
        final Template template = findOneById(templateId);

        this.templateRepository.delete(template);

        return new CommandProcessingResultBuilder().withEntityId(templateId)
                .build();
    }

    @Transactional
    @Override
    public Template updateTemplate(final Template template) {
        return this.templateRepository.saveAndFlush(template);
    }

    @Override
    public List<Template> getAllByEntityAndType(final TemplateEntity entity,
            final TemplateType type) {

        return this.templateRepository.findByEntityAndType(entity, type);
    }
}
