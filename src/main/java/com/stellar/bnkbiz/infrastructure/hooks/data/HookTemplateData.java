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
package com.stellar.bnkbiz.infrastructure.hooks.data;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public class HookTemplateData implements Serializable {

	private final Long id;
	private final String name;

	// associations
	private final List<Field> schema;

	public static HookTemplateData instance(final Long id, final String name,
			final List<Field> schema) {
		return new HookTemplateData(id, name, schema);
	}

	private HookTemplateData(final Long id, final String name,
			final List<Field> schema) {
		this.id = id;
		this.name = name;

		// associations
		this.schema = schema;
	}

	public Long getServiceId() {
		return this.id;
	}

}
