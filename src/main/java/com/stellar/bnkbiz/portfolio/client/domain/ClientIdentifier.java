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

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.apache.commons.lang3.StringUtils;
import com.stellar.bnkbiz.infrastructure.codes.domain.CodeValue;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.domain.AbstractAuditableCustom;
import com.stellar.bnkbiz.useradministration.domain.AppUser;

@Entity
@Table(name = "m_client_identifier", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "document_type_id", "document_key" }, name = "unique_identifier_key"),
        @UniqueConstraint(columnNames = { "client_id", "document_type_id" }, name = "unique_client_identifier") })
public class ClientIdentifier extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "document_type_id", nullable = false)
    private CodeValue documentType;

    @Column(name = "document_key", length = 1000)
    private String documentKey;

    @Column(name = "description", length = 1000)
    private String description;

    public static ClientIdentifier fromJson(final Client client, final CodeValue documentType, final JsonCommand command) {
        final String documentKey = command.stringValueOfParameterNamed("documentKey");
        final String description = command.stringValueOfParameterNamed("description");
        return new ClientIdentifier(client, documentType, documentKey, description);
    }

    protected ClientIdentifier() {
        //
    }

    private ClientIdentifier(final Client client, final CodeValue documentType, final String documentKey, final String description) {
        this.client = client;
        this.documentType = documentType;
        this.documentKey = StringUtils.defaultIfEmpty(documentKey, null);
        this.description = StringUtils.defaultIfEmpty(description, null);
    }

    public void update(final CodeValue documentType) {
        this.documentType = documentType;
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String documentTypeIdParamName = "documentTypeId";
        if (command.isChangeInLongParameterNamed(documentTypeIdParamName, this.documentType.getId())) {
            final Long newValue = command.longValueOfParameterNamed(documentTypeIdParamName);
            actualChanges.put(documentTypeIdParamName, newValue);
        }

        final String documentKeyParamName = "documentKey";
        if (command.isChangeInStringParameterNamed(documentKeyParamName, this.documentKey)) {
            final String newValue = command.stringValueOfParameterNamed(documentKeyParamName);
            actualChanges.put(documentKeyParamName, newValue);
            this.documentKey = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String descriptionParamName = "description";
        if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.description = StringUtils.defaultIfEmpty(newValue, null);
        }

        return actualChanges;
    }

    public String documentKey() {
        return this.documentKey;
    }

    public Long documentTypeId() {
        return this.documentType.getId();
    }
}