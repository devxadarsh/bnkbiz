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
package com.stellar.bnkbiz.portfolio.interestratechart.domain;

import static com.stellar.bnkbiz.portfolio.interestratechart.InterestIncentiveApiConstants.amountParamName;
import static com.stellar.bnkbiz.portfolio.interestratechart.InterestIncentiveApiConstants.attributeNameParamName;
import static com.stellar.bnkbiz.portfolio.interestratechart.InterestIncentiveApiConstants.attributeValueParamName;
import static com.stellar.bnkbiz.portfolio.interestratechart.InterestIncentiveApiConstants.conditionTypeParamName;
import static com.stellar.bnkbiz.portfolio.interestratechart.InterestIncentiveApiConstants.entityTypeParamName;
import static com.stellar.bnkbiz.portfolio.interestratechart.InterestIncentiveApiConstants.incentiveTypeparamName;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.DataValidatorBuilder;
import com.stellar.bnkbiz.portfolio.common.domain.ConditionType;
import com.stellar.bnkbiz.portfolio.interestratechart.incentive.InterestIncentiveAttributeName;
import com.stellar.bnkbiz.portfolio.interestratechart.incentive.InterestIncentiveEntityType;
import com.stellar.bnkbiz.portfolio.interestratechart.incentive.InterestIncentiveType;

@Embeddable
public class InterestIncentivesFields {

    @Column(name = "entiry_type", nullable = false)
    private Integer entityType;

    @Column(name = "attribute_name", nullable = false)
    private Integer attributeName;

    @Column(name = "condition_type", nullable = false)
    private Integer conditionType;

    @Column(name = "attribute_value", nullable = false)
    private String attributeValue;

    @Column(name = "incentive_type", nullable = false)
    private Integer incentiveType;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    protected InterestIncentivesFields() {

    }

    public static InterestIncentivesFields createNew(final Integer entityType, final Integer attributeName, final Integer conditionType,
            final String attributeValue, final Integer incentiveType, final BigDecimal amount, final DataValidatorBuilder baseDataValidator) {
        return new InterestIncentivesFields(entityType, attributeName, conditionType, attributeValue, incentiveType, amount,
                baseDataValidator);
    }

    private InterestIncentivesFields(final Integer entityType, final Integer attributeName, final Integer conditionType,
            final String attributeValue, final Integer incentiveType, final BigDecimal amount, final DataValidatorBuilder baseDataValidator) {
        this.entityType = entityType;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.conditionType = conditionType;
        this.incentiveType = incentiveType;
        this.amount = amount;
        validateIncentiveData(baseDataValidator);
    }

    public InterestIncentiveAttributeName attributeName() {
        return InterestIncentiveAttributeName.fromInt(this.attributeName);
    }

    public ConditionType conditionType() {
        return ConditionType.fromInt(this.conditionType);
    }

    public String attributeValue() {
        return this.attributeValue;
    }

    public InterestIncentiveType incentiveType() {
        return InterestIncentiveType.fromInt(this.incentiveType);
    }

    public BigDecimal amount() {
        return this.amount;
    }

    public InterestIncentiveEntityType entiryType() {
        return InterestIncentiveEntityType.fromInt(this.entityType);
    }

    public void update(final JsonCommand command, final Map<String, Object> actualChanges, final DataValidatorBuilder baseDataValidator,
            final Locale locale) {
        if (command.isChangeInIntegerParameterNamed(entityTypeParamName, this.entityType, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed(entityTypeParamName, locale);
            actualChanges.put(entityTypeParamName, newValue);
            this.entityType = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(attributeNameParamName, this.attributeName, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed(attributeNameParamName, locale);
            actualChanges.put(attributeNameParamName, newValue);
            this.attributeName = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(conditionTypeParamName, this.conditionType, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed(conditionTypeParamName, locale);
            actualChanges.put(conditionTypeParamName, newValue);
            this.conditionType = newValue;
        }

        if (command.isChangeInStringParameterNamed(attributeValueParamName, this.attributeValue)) {
            final String newValue = command.stringValueOfParameterNamed(attributeValueParamName);
            actualChanges.put(attributeValueParamName, newValue);
            this.attributeValue = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(incentiveTypeparamName, this.incentiveType, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed(incentiveTypeparamName, locale);
            actualChanges.put(incentiveTypeparamName, newValue);
            this.incentiveType = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(amountParamName, this.amount, locale)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(amountParamName, locale);
            actualChanges.put(amountParamName, newValue);
            this.amount = newValue;
        }

        validateIncentiveData(baseDataValidator);

    }

    public void validateIncentiveData(final DataValidatorBuilder baseDataValidator) {

        switch (attributeName()) {
            case GENDER:
                baseDataValidator.reset().parameter(attributeValueParamName).value(this.attributeValue).longGreaterThanZero();
                baseDataValidator.reset().parameter(conditionTypeParamName).value(this.conditionType)
                        .isOneOfTheseValues(ConditionType.EQUAL.getValue(), ConditionType.NOT_EQUAL.getValue());
            break;
            case AGE:
                baseDataValidator.reset().parameter(attributeValueParamName).value(this.attributeValue).longGreaterThanZero();
            break;
            case CLIENT_CLASSIFICATION:
                baseDataValidator.reset().parameter(attributeValueParamName).value(this.attributeValue).longGreaterThanZero();
                baseDataValidator.reset().parameter(conditionTypeParamName).value(this.conditionType)
                        .isOneOfTheseValues(ConditionType.EQUAL.getValue(), ConditionType.NOT_EQUAL.getValue());
            break;
            case CLIENT_TYPE:
                baseDataValidator.reset().parameter(attributeValueParamName).value(this.attributeValue).longGreaterThanZero();
                baseDataValidator.reset().parameter(conditionTypeParamName).value(this.conditionType)
                        .isOneOfTheseValues(ConditionType.EQUAL.getValue(), ConditionType.NOT_EQUAL.getValue());
            break;

            default:
            break;
        }
    }

}
