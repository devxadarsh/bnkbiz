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
package com.stellar.bnkbiz.portfolio.collectionsheet.serialization;

import java.math.BigDecimal;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import com.stellar.bnkbiz.infrastructure.core.exception.InvalidJsonException;
import com.stellar.bnkbiz.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import com.stellar.bnkbiz.infrastructure.core.serialization.FromApiJsonDeserializer;
import com.stellar.bnkbiz.infrastructure.core.serialization.FromJsonHelper;
import com.stellar.bnkbiz.portfolio.collectionsheet.command.CollectionSheetBulkRepaymentCommand;
import com.stellar.bnkbiz.portfolio.collectionsheet.command.SingleRepaymentCommand;
import com.stellar.bnkbiz.portfolio.paymentdetail.domain.PaymentDetail;
import com.stellar.bnkbiz.portfolio.paymentdetail.domain.PaymentDetailAssembler;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link CollectionSheetBulkRepaymentCommand}'s.
 */
@Component
public final class CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer extends
        AbstractFromApiJsonDeserializer<CollectionSheetBulkRepaymentCommand> {

    private final FromJsonHelper fromApiJsonHelper;
    private final PaymentDetailAssembler paymentDetailAssembler;

    @Autowired
    public CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper,
            final PaymentDetailAssembler paymentDetailAssembler) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.paymentDetailAssembler = paymentDetailAssembler;
    }

    @Override
    public CollectionSheetBulkRepaymentCommand commandFromApiJson(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final PaymentDetail paymentDetail = this.paymentDetailAssembler.fetchPaymentDetail(element.getAsJsonObject());

        return commandFromApiJson(json, paymentDetail);
    }

    public CollectionSheetBulkRepaymentCommand commandFromApiJson(final String json, final PaymentDetail paymentDetail) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);

        final String note = this.fromApiJsonHelper.extractStringNamed("note", element);

        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

        SingleRepaymentCommand[] loanRepaymentTransactions = null;

        if (element.isJsonObject()) {
            if (topLevelJsonElement.has("bulkRepaymentTransactions") && topLevelJsonElement.get("bulkRepaymentTransactions").isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get("bulkRepaymentTransactions").getAsJsonArray();
                loanRepaymentTransactions = new SingleRepaymentCommand[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject loanTransactionElement = array.get(i).getAsJsonObject();

                    final Long loanId = this.fromApiJsonHelper.extractLongNamed("loanId", loanTransactionElement);
                    final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalNamed("transactionAmount",
                            loanTransactionElement, locale);
                    PaymentDetail detail = paymentDetail;
                    if (paymentDetail == null) {
                        detail = this.paymentDetailAssembler.fetchPaymentDetail(loanTransactionElement);
                    }
                    if(transactionAmount != null && transactionAmount.intValue() > 0){
                    	loanRepaymentTransactions[i] = new SingleRepaymentCommand(loanId, transactionAmount, transactionDate, detail);
                    }
                }
            }
        }
        return new CollectionSheetBulkRepaymentCommand(note, transactionDate, loanRepaymentTransactions);
    }

}