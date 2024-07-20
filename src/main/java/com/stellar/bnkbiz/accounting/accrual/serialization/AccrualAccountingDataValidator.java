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
package com.stellar.bnkbiz.accounting.accrual.serialization;

import static com.stellar.bnkbiz.accounting.accrual.api.AccrualAccountingConstants.LOAN_PERIODIC_REQUEST_DATA_PARAMETERS;
import static com.stellar.bnkbiz.accounting.accrual.api.AccrualAccountingConstants.PERIODIC_ACCRUAL_ACCOUNTING_RESOURCE_NAME;
import static com.stellar.bnkbiz.accounting.accrual.api.AccrualAccountingConstants.accrueTillParamName;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import org.apache.commons.lang3.StringUtils;
import com.stellar.bnkbiz.infrastructure.core.data.ApiParameterError;
import com.stellar.bnkbiz.infrastructure.core.data.DataValidatorBuilder;
import com.stellar.bnkbiz.infrastructure.core.exception.InvalidJsonException;
import com.stellar.bnkbiz.infrastructure.core.exception.PlatformApiDataValidationException;
import com.stellar.bnkbiz.infrastructure.core.serialization.FromApiJsonDeserializer;
import com.stellar.bnkbiz.infrastructure.core.serialization.FromJsonHelper;
import com.stellar.bnkbiz.infrastructure.core.service.DateUtils;
import com.stellar.bnkbiz.portfolio.loanaccount.guarantor.command.GuarantorCommand;
import org.apache.commons.lang3.StringUtils;
//import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link GuarantorCommand}'s.
 */
@Component
public final class AccrualAccountingDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public AccrualAccountingDataValidator(final FromJsonHelper fromApiJsonfromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonfromApiJsonHelper;
    }

    public void validateLoanPeriodicAccrualData(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, LOAN_PERIODIC_REQUEST_DATA_PARAMETERS);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(PERIODIC_ACCRUAL_ACCOUNTING_RESOURCE_NAME);

        final LocalDate date = this.fromApiJsonHelper.extractLocalDateNamed(accrueTillParamName, element);
        baseDataValidator.reset().parameter(accrueTillParamName).value(date).notNull().validateDateBefore(DateUtils.getLocalDateOfTenant());

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}