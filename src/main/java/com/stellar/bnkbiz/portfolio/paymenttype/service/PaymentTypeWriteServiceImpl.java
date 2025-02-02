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
package com.stellar.bnkbiz.portfolio.paymenttype.service;

import java.util.Map;

import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResultBuilder;
import com.stellar.bnkbiz.infrastructure.core.exception.PlatformDataIntegrityException;
import com.stellar.bnkbiz.portfolio.paymenttype.api.PaymentTypeApiResourceConstants;
import com.stellar.bnkbiz.portfolio.paymenttype.data.PaymentTypeDataValidator;
import com.stellar.bnkbiz.portfolio.paymenttype.domain.PaymentType;
import com.stellar.bnkbiz.portfolio.paymenttype.domain.PaymentTypeRepository;
import com.stellar.bnkbiz.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class PaymentTypeWriteServiceImpl implements PaymentTypeWriteService {

    private final PaymentTypeRepository repository;
    private final PaymentTypeRepositoryWrapper repositoryWrapper;
    private final PaymentTypeDataValidator fromApiJsonDeserializer;

    @Autowired
    public PaymentTypeWriteServiceImpl(PaymentTypeRepository repository, PaymentTypeRepositoryWrapper repositoryWrapper,
            PaymentTypeDataValidator fromApiJsonDeserializer) {
        this.repository = repository;
        this.repositoryWrapper = repositoryWrapper;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;

    }

    @Override
    public CommandProcessingResult createPaymentType(JsonCommand command) {
        this.fromApiJsonDeserializer.validateForCreate(command.json());
        String name = command.stringValueOfParameterNamed(PaymentTypeApiResourceConstants.NAME);
        String description = command.stringValueOfParameterNamed(PaymentTypeApiResourceConstants.DESCRIPTION);
        Boolean isCashPayment = command.booleanObjectValueOfParameterNamed(PaymentTypeApiResourceConstants.ISCASHPAYMENT);
        Long position = command.longValueOfParameterNamed(PaymentTypeApiResourceConstants.POSITION);

        PaymentType newPaymentType = PaymentType.create(name, description, isCashPayment, position);
        this.repository.save(newPaymentType);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(newPaymentType.getId()).build();
    }

    @Override
    public CommandProcessingResult updatePaymentType(Long paymentTypeId, JsonCommand command) {

        this.fromApiJsonDeserializer.validateForUpdate(command.json());
        final PaymentType paymentType = this.repositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);
        final Map<String, Object> changes = paymentType.update(command);

        if (!changes.isEmpty()) {
            this.repository.save(paymentType);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(command.entityId()).build();
    }

    @Override
    public CommandProcessingResult deletePaymentType(Long paymentTypeId) {
        final PaymentType paymentType = this.repositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);
        try {
            this.repository.delete(paymentType);
            this.repository.flush();
        } catch (final DataIntegrityViolationException e) {
            handleDataIntegrityIssues(e);
        }
        return new CommandProcessingResultBuilder().withEntityId(paymentType.getId()).build();
    }

    private void handleDataIntegrityIssues(final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("acc_product_mapping")) {
            throw new PlatformDataIntegrityException("error.msg.payment.type.association.exist",
                    "cannot.delete.payment.type.with.association");
        } else if (realCause.getMessage().contains("payment_type_id")) { throw new PlatformDataIntegrityException(
                "error.msg.payment.type.association.exist", "cannot.delete.payment.type.with.association"); }

        throw new PlatformDataIntegrityException("error.msg.paymenttypes.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }
}
