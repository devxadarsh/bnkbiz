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
package com.stellar.bnkbiz.portfolio.collectionsheet.data;

import static com.stellar.bnkbiz.portfolio.collectionsheet.CollectionSheetConstants.COLLECTIONSHEET_REQUEST_DATA_PARAMETERS;
import static com.stellar.bnkbiz.portfolio.collectionsheet.CollectionSheetConstants.COLLECTIONSHEET_RESOURCE_NAME;
import static com.stellar.bnkbiz.portfolio.collectionsheet.CollectionSheetConstants.INDIVIDUAL_COLLECTIONSHEET_REQUEST_DATA_PARAMETERS;
import static com.stellar.bnkbiz.portfolio.collectionsheet.CollectionSheetConstants.attendanceTypeParamName;
import static com.stellar.bnkbiz.portfolio.collectionsheet.CollectionSheetConstants.bulkDisbursementTransactionsParamName;
import static com.stellar.bnkbiz.portfolio.collectionsheet.CollectionSheetConstants.bulkRepaymentTransactionsParamName;
import static com.stellar.bnkbiz.portfolio.collectionsheet.CollectionSheetConstants.bulkSavingsDueTransactionsParamName;
import static com.stellar.bnkbiz.portfolio.collectionsheet.CollectionSheetConstants.calendarIdParamName;
import static com.stellar.bnkbiz.portfolio.collectionsheet.CollectionSheetConstants.clientIdParamName;
import static com.stellar.bnkbiz.portfolio.collectionsheet.CollectionSheetConstants.clientsAttendanceParamName;
import static com.stellar.bnkbiz.portfolio.collectionsheet.CollectionSheetConstants.loanIdParamName;
import static com.stellar.bnkbiz.portfolio.collectionsheet.CollectionSheetConstants.noteParamName;
import static com.stellar.bnkbiz.portfolio.collectionsheet.CollectionSheetConstants.savingsIdParamName;
import static com.stellar.bnkbiz.portfolio.collectionsheet.CollectionSheetConstants.transactionAmountParamName;
import static com.stellar.bnkbiz.portfolio.collectionsheet.CollectionSheetConstants.transactionDateParamName;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.ApiParameterError;
import com.stellar.bnkbiz.infrastructure.core.data.DataValidatorBuilder;
import com.stellar.bnkbiz.infrastructure.core.exception.InvalidJsonException;
import com.stellar.bnkbiz.infrastructure.core.exception.PlatformApiDataValidationException;
import com.stellar.bnkbiz.infrastructure.core.serialization.FromJsonHelper;
import com.stellar.bnkbiz.portfolio.paymentdetail.PaymentDetailConstants;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
public class CollectionSheetTransactionDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CollectionSheetTransactionDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateTransaction(final JsonCommand command) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, COLLECTIONSHEET_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(COLLECTIONSHEET_RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(transactionDateParamName, element);
        baseDataValidator.reset().parameter(transactionDateParamName).value(transactionDate).notNull();

        final String note = this.fromApiJsonHelper.extractStringNamed(noteParamName, element);
        if (StringUtils.isNotBlank(note)) {
            baseDataValidator.reset().parameter(noteParamName).value(note).notExceedingLengthOf(1000);
        }

        final Long calendarId = this.fromApiJsonHelper.extractLongNamed(calendarIdParamName, element);
        baseDataValidator.reset().parameter(calendarIdParamName).value(calendarId).notNull();

        validateAttendanceDetails(element, baseDataValidator);

        validateDisbursementTransactions(element, baseDataValidator);

        validateRepaymentTransactions(element, baseDataValidator);

        validateSavingsDueTransactions(element, baseDataValidator);
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
        validatePaymentDetails(baseDataValidator, element, locale);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateIndividualCollectionSheet(final JsonCommand command) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, INDIVIDUAL_COLLECTIONSHEET_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(COLLECTIONSHEET_RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(transactionDateParamName, element);
        baseDataValidator.reset().parameter(transactionDateParamName).value(transactionDate).notNull();

        final String note = this.fromApiJsonHelper.extractStringNamed(noteParamName, element);
        if (StringUtils.isNotBlank(note)) {
            baseDataValidator.reset().parameter(noteParamName).value(note).notExceedingLengthOf(1000);
        }

        validateDisbursementTransactions(element, baseDataValidator);

        validateRepaymentTransactions(element, baseDataValidator);

        validateSavingsDueTransactions(element, baseDataValidator);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateAttendanceDetails(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        if (element.isJsonObject()) {
            if (topLevelJsonElement.has(clientsAttendanceParamName) && topLevelJsonElement.get(clientsAttendanceParamName).isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(clientsAttendanceParamName).getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject attendanceElement = array.get(i).getAsJsonObject();
                    final Long clientId = this.fromApiJsonHelper.extractLongNamed(clientIdParamName, attendanceElement);
                    final Long attendanceType = this.fromApiJsonHelper.extractLongNamed(attendanceTypeParamName, attendanceElement);
                    baseDataValidator.reset().parameter(clientsAttendanceParamName + "[" + i + "]." + clientIdParamName).value(clientId)
                            .notNull().integerGreaterThanZero();
                    baseDataValidator.reset().parameter(clientsAttendanceParamName + "[" + i + "]." + attendanceTypeParamName)
                            .value(attendanceType).notNull().integerGreaterThanZero();
                }
            }
        }
    }

    private void validateDisbursementTransactions(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
        if (element.isJsonObject()) {
            if (topLevelJsonElement.has(bulkDisbursementTransactionsParamName)
                    && topLevelJsonElement.get(bulkDisbursementTransactionsParamName).isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(bulkDisbursementTransactionsParamName).getAsJsonArray();

                for (int i = 0; i < array.size(); i++) {
                    final JsonObject loanTransactionElement = array.get(i).getAsJsonObject();
                    final Long loanId = this.fromApiJsonHelper.extractLongNamed(loanIdParamName, loanTransactionElement);
                    final BigDecimal disbursementAmount = this.fromApiJsonHelper.extractBigDecimalNamed(transactionAmountParamName,
                            loanTransactionElement, locale);

                    baseDataValidator.reset().parameter("bulktransaction" + "[" + i + "].loan.id").value(loanId).notNull()
                            .integerGreaterThanZero();
                    baseDataValidator.reset().parameter("bulktransaction" + "[" + i + "].disbursement.amount").value(disbursementAmount)
                            .notNull().zeroOrPositiveAmount();
                }
            }
        }
    }

    private void validateRepaymentTransactions(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
        if (element.isJsonObject()) {
            if (topLevelJsonElement.has(bulkRepaymentTransactionsParamName)
                    && topLevelJsonElement.get(bulkRepaymentTransactionsParamName).isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(bulkRepaymentTransactionsParamName).getAsJsonArray();

                for (int i = 0; i < array.size(); i++) {
                    final JsonObject loanTransactionElement = array.get(i).getAsJsonObject();
                    final Long loanId = this.fromApiJsonHelper.extractLongNamed(loanIdParamName, loanTransactionElement);
                    final BigDecimal disbursementAmount = this.fromApiJsonHelper.extractBigDecimalNamed(transactionAmountParamName,
                            loanTransactionElement, locale);

                    baseDataValidator.reset().parameter("bulktransaction" + "[" + i + "].loan.id").value(loanId).notNull()
                            .integerGreaterThanZero();
                    baseDataValidator.reset().parameter("bulktransaction" + "[" + i + "].disbursement.amount").value(disbursementAmount)
                            .notNull().zeroOrPositiveAmount();

                    validatePaymentDetails(baseDataValidator, loanTransactionElement, locale);
                }
            }
        }
    }

    private void validateSavingsDueTransactions(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
        if (element.isJsonObject()) {
            if (topLevelJsonElement.has(bulkSavingsDueTransactionsParamName)
                    && topLevelJsonElement.get(bulkSavingsDueTransactionsParamName).isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(bulkSavingsDueTransactionsParamName).getAsJsonArray();

                for (int i = 0; i < array.size(); i++) {
                    final JsonObject savingsTransactionElement = array.get(i).getAsJsonObject();
                    final Long savingsId = this.fromApiJsonHelper.extractLongNamed(savingsIdParamName, savingsTransactionElement);
                    final BigDecimal dueAmount = this.fromApiJsonHelper.extractBigDecimalNamed(transactionAmountParamName,
                            savingsTransactionElement, locale);

                    baseDataValidator.reset().parameter("bulktransaction" + "[" + i + "].savings.id").value(savingsId).notNull()
                            .integerGreaterThanZero();
                    baseDataValidator.reset().parameter("bulktransaction" + "[" + i + "].due.amount").value(dueAmount).notNull()
                            .zeroOrPositiveAmount();
                    validatePaymentDetails(baseDataValidator, savingsTransactionElement, locale);
                }
            }
        }
    }

    private void validatePaymentDetails(final DataValidatorBuilder baseDataValidator, final JsonElement element, final Locale locale) {
        // Validate all string payment detail fields for max length
        final Integer paymentTypeId = this.fromApiJsonHelper.extractIntegerNamed(PaymentDetailConstants.paymentTypeParamName, element,
                locale);
        baseDataValidator.reset().parameter(PaymentDetailConstants.paymentTypeParamName).value(paymentTypeId).ignoreIfNull()
                .integerGreaterThanZero();
        for (final String paymentDetailParameterName : PaymentDetailConstants.PAYMENT_CREATE_REQUEST_DATA_PARAMETERS) {
            final String paymentDetailParameterValue = this.fromApiJsonHelper.extractStringNamed(paymentDetailParameterName, element);
            baseDataValidator.reset().parameter(paymentDetailParameterName).value(paymentDetailParameterValue).ignoreIfNull()
                    .notExceedingLengthOf(50);
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}
