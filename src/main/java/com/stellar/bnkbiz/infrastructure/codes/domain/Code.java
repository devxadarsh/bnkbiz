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
package com.stellar.bnkbiz.infrastructure.codes.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.apache.commons.lang3.StringUtils;
import com.stellar.bnkbiz.infrastructure.codes.exception.SystemDefinedCodeCannotBeChangedException;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_code", uniqueConstraints = { @UniqueConstraint(columnNames = { "code_name" }, name = "code_name") })
public class Code extends AbstractPersistable<Long> {

    @Column(name = "code_name", length = 100)
    private String name;

    @Column(name = "is_system_defined")
    private final boolean systemDefined;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "code", orphanRemoval = true)
    private Set<CodeValue> values;

    public static Code fromJson(final JsonCommand command) {
        final String name = command.stringValueOfParameterNamed("name");
        return new Code(name);
    }
    
    public static Code createNew(final String name) {
        return new Code(name);
    }

    protected Code() {
        this.systemDefined = false;
    }

    private Code(final String name) {
        this.name = name;
        this.systemDefined = false;
    }

    public String name() {
        return this.name;
    }

    public boolean isSystemDefined() {
        return this.systemDefined;
    }

    public Map<String, Object> update(final JsonCommand command) {

        if (this.systemDefined) { throw new SystemDefinedCodeCannotBeChangedException(); }

        final Map<String, Object> actualChanges = new LinkedHashMap<>(1);

        final String firstnameParamName = "name";
        if (command.isChangeInStringParameterNamed(firstnameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(firstnameParamName);
            actualChanges.put(firstnameParamName, newValue);
            this.name = StringUtils.defaultIfEmpty(newValue, null);
        }

        return actualChanges;
    }

    public boolean remove(final CodeValue codeValueToDelete) {
        return this.values.remove(codeValueToDelete);
    }
}