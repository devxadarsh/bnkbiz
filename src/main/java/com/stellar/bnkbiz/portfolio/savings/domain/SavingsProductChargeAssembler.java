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
package com.stellar.bnkbiz.portfolio.savings.domain;

import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.amountParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.chargeCalculationTypeParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.chargeIdParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.chargeTimeTypeParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.chargesParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.dueAsOfDateParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.feeIntervalParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.feeOnMonthDayParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.idParamName;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.stellar.bnkbiz.infrastructure.core.serialization.FromJsonHelper;
import com.stellar.bnkbiz.portfolio.charge.domain.Charge;
import com.stellar.bnkbiz.portfolio.charge.domain.ChargeCalculationType;
import com.stellar.bnkbiz.portfolio.charge.domain.ChargeRepositoryWrapper;
import com.stellar.bnkbiz.portfolio.charge.domain.ChargeTimeType;
import com.stellar.bnkbiz.portfolio.charge.exception.SavingsAccountChargeNotFoundException;
import java.time.LocalDate;
import org.joda.time.MonthDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class SavingsProductChargeAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final ChargeRepositoryWrapper chargeRepository;
    private final SavingsAccountChargeRepository savingsAccountChargeRepository;

    @Autowired
    public SavingsProductChargeAssembler(final FromJsonHelper fromApiJsonHelper, final ChargeRepositoryWrapper chargeRepository,
            final SavingsAccountChargeRepository savingsAccountChargeRepository) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.chargeRepository = chargeRepository;
        this.savingsAccountChargeRepository = savingsAccountChargeRepository;
    }

    public Set<SavingsAccountCharge> fromParsedJson(final JsonElement element) {

        final Set<SavingsAccountCharge> savingsAccountCharges = new HashSet<>();

        if (element.isJsonObject()) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
            if (topLevelJsonElement.has(chargesParamName) && topLevelJsonElement.get(chargesParamName).isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(chargesParamName).getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {

                    final JsonObject savingsChargeElement = array.get(i).getAsJsonObject();

                    final Long id = this.fromApiJsonHelper.extractLongNamed(idParamName, savingsChargeElement);
                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed(chargeIdParamName, savingsChargeElement);
                    final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed(amountParamName, savingsChargeElement, locale);
                    final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerNamed(chargeTimeTypeParamName,
                            savingsChargeElement, locale);
                    final Integer chargeCalculationType = this.fromApiJsonHelper.extractIntegerNamed(chargeCalculationTypeParamName,
                            savingsChargeElement, locale);
                    final LocalDate dueDate = this.fromApiJsonHelper.extractLocalDateNamed(dueAsOfDateParamName, savingsChargeElement,
                            dateFormat, locale);
                    final MonthDay feeOnMonthDay = this.fromApiJsonHelper
                            .extractMonthDayNamed(feeOnMonthDayParamName, savingsChargeElement);
                    final Integer feeInterval = this.fromApiJsonHelper.extractIntegerNamed(feeIntervalParamName, savingsChargeElement,
                            locale);

                    if (id == null) {
                        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeId);
                        final ChargeTimeType chargeTime = null;
                        if (chargeTimeType != null) {
                            ChargeTimeType.fromInt(chargeTimeType);
                        }
                        final ChargeCalculationType chargeCalculation = null;
                        if (chargeCalculationType != null) {
                            ChargeCalculationType.fromInt(chargeCalculationType);
                        }
                        final SavingsAccountCharge savingsAccountCharge = SavingsAccountCharge.createNewWithoutSavingsAccount(
                                chargeDefinition, amount, chargeTime, chargeCalculation, dueDate, true, feeOnMonthDay, feeInterval);
                        savingsAccountCharges.add(savingsAccountCharge);
                    } else {
                        final Long savingsAccountChargeId = id;
                        final SavingsAccountCharge savingsAccountCharge = this.savingsAccountChargeRepository
                                .findOne(savingsAccountChargeId);
                        if (savingsAccountCharge == null) { throw new SavingsAccountChargeNotFoundException(savingsAccountChargeId); }

                        savingsAccountCharge.update(amount, dueDate, null, null);

                        savingsAccountCharges.add(savingsAccountCharge);
                    }
                }
            }
        }

        return savingsAccountCharges;
    }

}