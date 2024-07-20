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
package com.stellar.bnkbiz.infrastructure.hooks.domain;

import static com.stellar.bnkbiz.infrastructure.hooks.api.HookApiConstants.nameParamName;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_hook_templates")
public class HookTemplate extends AbstractPersistable<Long> {

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "template", orphanRemoval = true)
	private final Set<Schema> fields = new HashSet<>();

	private HookTemplate(final String name) {

		if (StringUtils.isNotBlank(name)) {
			this.name = name.trim();
		} else {
			this.name = null;
		}
	}

	protected HookTemplate() {

	}

	public static HookTemplate fromJson(final JsonCommand command) {
		final String name = command.stringValueOfParameterNamed(nameParamName);
		return new HookTemplate(name);
	}

	public String getName() {
		return this.name;
	}

	public Set<Schema> getSchema() {
		return this.fields;
	}
}
