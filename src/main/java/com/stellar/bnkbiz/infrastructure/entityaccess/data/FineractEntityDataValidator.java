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
package com.stellar.bnkbiz.infrastructure.entityaccess.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import com.stellar.bnkbiz.infrastructure.core.data.ApiParameterError;
import com.stellar.bnkbiz.infrastructure.core.data.DataValidatorBuilder;
import com.stellar.bnkbiz.infrastructure.core.exception.InvalidJsonException;
import com.stellar.bnkbiz.infrastructure.core.exception.PlatformApiDataValidationException;
import com.stellar.bnkbiz.infrastructure.core.serialization.FromJsonHelper;
import com.stellar.bnkbiz.infrastructure.entityaccess.api.FineractEntityApiResourceConstants;
import com.stellar.bnkbiz.organisation.office.domain.OfficeRepositoryWrapper;
import com.stellar.bnkbiz.portfolio.charge.domain.ChargeRepositoryWrapper;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.LoanProduct;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.LoanProductRepository;
import com.stellar.bnkbiz.portfolio.loanproduct.exception.LoanProductNotFoundException;
import com.stellar.bnkbiz.portfolio.savings.domain.SavingsProduct;
import com.stellar.bnkbiz.portfolio.savings.domain.SavingsProductRepository;
import com.stellar.bnkbiz.portfolio.savings.exception.SavingsProductNotFoundException;
import com.stellar.bnkbiz.useradministration.domain.Role;
import com.stellar.bnkbiz.useradministration.domain.RoleRepository;
import com.stellar.bnkbiz.useradministration.exception.RoleNotFoundException;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class FineractEntityDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final LoanProductRepository loanProductRepository;
    private final SavingsProductRepository savingsProductRepository;
    private final ChargeRepositoryWrapper chargeRepositoryWrapper;
    private final RoleRepository roleRepository;

    @Autowired
    public FineractEntityDataValidator(final FromJsonHelper fromApiJsonHelper, final OfficeRepositoryWrapper officeRepositoryWrapper,
            final LoanProductRepository loanProductRepository, final SavingsProductRepository savingsProductRepository,
            final ChargeRepositoryWrapper chargeRepositoryWrapper, final RoleRepository roleRepository) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.officeRepositoryWrapper = officeRepositoryWrapper;
        this.loanProductRepository = loanProductRepository;
        this.savingsProductRepository = savingsProductRepository;
        this.chargeRepositoryWrapper = chargeRepositoryWrapper;
        this.roleRepository = roleRepository;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                FineractEntityApiResourceConstants.CREATE_ENTITY_MAPPING_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(FineractEntityApiResourceConstants.FINERACT_ENTITY_RESOURCE_NAME);

        if (this.fromApiJsonHelper.parameterExists(FineractEntityApiResourceConstants.fromEnityType, element)) {
            final Long fromId = this.fromApiJsonHelper.extractLongNamed(FineractEntityApiResourceConstants.fromEnityType, element);
            baseDataValidator.reset().parameter(FineractEntityApiResourceConstants.fromEnityType).value(fromId).notNull()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(FineractEntityApiResourceConstants.toEntityType, element)) {
            final Long toId = this.fromApiJsonHelper.extractLongNamed(FineractEntityApiResourceConstants.toEntityType, element);
            baseDataValidator.reset().parameter(FineractEntityApiResourceConstants.toEntityType).value(toId).notNull()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(FineractEntityApiResourceConstants.startDate, element)) {
            final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(FineractEntityApiResourceConstants.startDate, element);
            baseDataValidator.reset().parameter(FineractEntityApiResourceConstants.startDate).value(startDate);
        }

        if (this.fromApiJsonHelper.parameterExists(FineractEntityApiResourceConstants.endDate, element)) {
            final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed(FineractEntityApiResourceConstants.endDate, element);
            baseDataValidator.reset().parameter(FineractEntityApiResourceConstants.endDate).value(endDate);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void checkForEntity(String relId, Long fromId, Long toId) {

        switch (relId) {
            case "1":
                checkForOffice(fromId);
                checkForLoanProducts(toId);
            break;
            case "2":
                checkForOffice(fromId);
                checkForSavingsProducts(toId);
            break;
            case "3":
                checkForOffice(fromId);
                checkForCharges(toId);
            break;
            case "4":
                checkForRoles(fromId);
                checkForLoanProducts(toId);
            break;
            case "5":
                checkForRoles(fromId);
                checkForSavingsProducts(toId);
            break;

        }

    }

    public void checkForOffice(Long id) {
        this.officeRepositoryWrapper.findOneWithNotFoundDetection(id);
    }

    public void checkForLoanProducts(final Long id) {
        final LoanProduct loanProduct = this.loanProductRepository.findOne(id);
        if (loanProduct == null) { throw new LoanProductNotFoundException(id); }
    }

    public void checkForSavingsProducts(final Long id) {
        final SavingsProduct savingsProduct = this.savingsProductRepository.findOne(id);
        if (savingsProduct == null) { throw new SavingsProductNotFoundException(id); }
    }

    public void checkForCharges(final Long id) {
        this.chargeRepositoryWrapper.findOneWithNotFoundDetection(id);
    }

    public void checkForRoles(final Long id) {
        final Role role = this.roleRepository.findOne(id);
        if (role == null) { throw new RoleNotFoundException(id); }
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                FineractEntityApiResourceConstants.UPDATE_ENTITY_MAPPING_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(FineractEntityApiResourceConstants.FINERACT_ENTITY_RESOURCE_NAME);

        boolean atLeastOneParameterPassedForUpdate = false;
        if (this.fromApiJsonHelper.parameterExists(FineractEntityApiResourceConstants.fromEnityType, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String fromEnityType = this.fromApiJsonHelper.extractStringNamed(FineractEntityApiResourceConstants.fromEnityType, element);
            baseDataValidator.reset().parameter(FineractEntityApiResourceConstants.fromEnityType).value(fromEnityType);
        }

        if (this.fromApiJsonHelper.parameterExists(FineractEntityApiResourceConstants.fromEnityType, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String toEnityType = this.fromApiJsonHelper.extractStringNamed(FineractEntityApiResourceConstants.toEntityType, element);
            baseDataValidator.reset().parameter(FineractEntityApiResourceConstants.fromEnityType).value(toEnityType);
        }

        if (this.fromApiJsonHelper.parameterExists(FineractEntityApiResourceConstants.toEntityType, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (this.fromApiJsonHelper.parameterExists(FineractEntityApiResourceConstants.startDate, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (this.fromApiJsonHelper.parameterExists(FineractEntityApiResourceConstants.endDate, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (!atLeastOneParameterPassedForUpdate) {
            final Object forceError = null;
            baseDataValidator.reset().anyOfNotNull(forceError);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

}
