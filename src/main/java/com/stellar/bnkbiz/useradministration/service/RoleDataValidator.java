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
package com.stellar.bnkbiz.useradministration.service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import com.stellar.bnkbiz.infrastructure.core.data.ApiParameterError;
import com.stellar.bnkbiz.infrastructure.core.data.DataValidatorBuilder;
import com.stellar.bnkbiz.infrastructure.core.exception.InvalidJsonException;
import com.stellar.bnkbiz.infrastructure.core.exception.PlatformApiDataValidationException;
import com.stellar.bnkbiz.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class RoleDataValidator {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<>(Arrays.asList("id", "name", "description"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public RoleDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("role");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed("name", element);
        baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);

        final String description = this.fromApiJsonHelper.extractStringNamed("description", element);
        baseDataValidator.reset().parameter("description").value(description).notBlank().notExceedingLengthOf(500);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("role");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists("name", element)) {
            final String username = this.fromApiJsonHelper.extractStringNamed("name", element);
            baseDataValidator.reset().parameter("name").value(username).notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists("description", element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed("description", element);
            baseDataValidator.reset().parameter("description").value(description).notBlank().notExceedingLengthOf(500);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}