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
package com.stellar.bnkbiz.infrastructure.configuration.domain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "c_external_service", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "name_UNIQUE") })
public class ExternalService extends AbstractPersistable<Long> {

    @Column(name = "name", length = 50)
    private String name;

    // @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy =
    // "externalServicePropertiesPK.externalService", orphanRemoval = true)
    // private Set<ExternalServicesProperties> values;

    public static ExternalService fromJson(final JsonCommand command) {
        final String name = command.stringValueOfParameterNamed("name");
        return new ExternalService(name);
    }

    private ExternalService(final String name) {
        this.name = name;
    }

    protected ExternalService() {}

    public String name() {
        return this.name;
    }

}