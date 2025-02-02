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
package com.stellar.bnkbiz.portfolio.paymentdetail.domain;

import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.portfolio.paymentdetail.PaymentDetailConstants;
import com.stellar.bnkbiz.portfolio.paymentdetail.data.PaymentDetailData;
import com.stellar.bnkbiz.portfolio.paymenttype.data.PaymentTypeData;
import com.stellar.bnkbiz.portfolio.paymenttype.domain.PaymentType;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_payment_detail")
public final class PaymentDetail extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "payment_type_id", nullable = false)
    private PaymentType paymentType;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "check_number", length = 50)
    private String checkNumber;

    @Column(name = "routing_code", length = 50)
    private String routingCode;

    @Column(name = "receipt_number", length = 50)
    private String receiptNumber;

    @Column(name = "bank_number", length = 50)
    private String bankNumber;

    protected PaymentDetail() {

    }

    public static PaymentDetail generatePaymentDetail(final PaymentType paymentType, final JsonCommand command,
            final Map<String, Object> changes) {
        final String accountNumber = command.stringValueOfParameterNamed(PaymentDetailConstants.accountNumberParamName);
        final String checkNumber = command.stringValueOfParameterNamed(PaymentDetailConstants.checkNumberParamName);
        final String routingCode = command.stringValueOfParameterNamed(PaymentDetailConstants.routingCodeParamName);
        final String receiptNumber = command.stringValueOfParameterNamed(PaymentDetailConstants.receiptNumberParamName);
        final String bankNumber = command.stringValueOfParameterNamed(PaymentDetailConstants.bankNumberParamName);

        if (StringUtils.isNotBlank(accountNumber)) {
            changes.put(PaymentDetailConstants.accountNumberParamName, accountNumber);
        }
        if (StringUtils.isNotBlank(checkNumber)) {
            changes.put(PaymentDetailConstants.checkNumberParamName, checkNumber);
        }
        if (StringUtils.isNotBlank(routingCode)) {
            changes.put(PaymentDetailConstants.routingCodeParamName, routingCode);
        }
        if (StringUtils.isNotBlank(receiptNumber)) {
            changes.put(PaymentDetailConstants.receiptNumberParamName, receiptNumber);
        }
        if (StringUtils.isNotBlank(bankNumber)) {
            changes.put(PaymentDetailConstants.bankNumberParamName, bankNumber);
        }
        final PaymentDetail paymentDetail = new PaymentDetail(paymentType, accountNumber, checkNumber, routingCode, receiptNumber,
                bankNumber);
        return paymentDetail;
    }

    public static PaymentDetail instance(final PaymentType paymentType, final String accountNumber, final String checkNumber,
            final String routingCode, final String receiptNumber, final String bankNumber) {
        return new PaymentDetail(paymentType, accountNumber, checkNumber, routingCode, receiptNumber, bankNumber);
    }

    private PaymentDetail(final PaymentType paymentType, final String accountNumber, final String checkNumber, final String routingCode,
            final String receiptNumber, final String bankNumber) {
        this.paymentType = paymentType;
        this.accountNumber = accountNumber;
        this.checkNumber = checkNumber;
        this.routingCode = routingCode;
        this.receiptNumber = receiptNumber;
        this.bankNumber = bankNumber;
    }

    public PaymentDetailData toData() {
        final PaymentTypeData paymentTypeData = this.paymentType.toData();
        final PaymentDetailData paymentDetailData = new PaymentDetailData(getId(), paymentTypeData, this.accountNumber, this.checkNumber,
                this.routingCode, this.receiptNumber, this.bankNumber);
        return paymentDetailData;
    }

    public PaymentType getPaymentType() {
        return this.paymentType;
    }

}