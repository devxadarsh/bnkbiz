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
package com.stellar.bnkbiz.portfolio.savings.service;

import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.accountingRuleParamName;
import static com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants.chargesParamName;

import java.util.Map;
import java.util.Set;

import com.stellar.bnkbiz.accounting.producttoaccountmapping.service.ProductToGLAccountMappingWritePlatformService;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResultBuilder;
import com.stellar.bnkbiz.infrastructure.core.exception.PlatformDataIntegrityException;
import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
import com.stellar.bnkbiz.portfolio.charge.domain.Charge;
import com.stellar.bnkbiz.portfolio.interestratechart.service.InterestRateChartAssembler;
import com.stellar.bnkbiz.portfolio.savings.DepositAccountType;
import com.stellar.bnkbiz.portfolio.savings.data.DepositProductDataValidator;
import com.stellar.bnkbiz.portfolio.savings.domain.DepositProductAssembler;
import com.stellar.bnkbiz.portfolio.savings.domain.FixedDepositProduct;
import com.stellar.bnkbiz.portfolio.savings.domain.FixedDepositProductRepository;
import com.stellar.bnkbiz.portfolio.savings.exception.FixedDepositProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FixedDepositProductWritePlatformServiceJpaRepositoryImpl implements FixedDepositProductWritePlatformService {

    private final Logger logger;
    private final PlatformSecurityContext context;
    private final FixedDepositProductRepository fixedDepositProductRepository;
    private final DepositProductDataValidator fromApiJsonDataValidator;
    private final DepositProductAssembler depositProductAssembler;
    private final ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService;
    private final InterestRateChartAssembler chartAssembler;

    @Autowired
    public FixedDepositProductWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final FixedDepositProductRepository fixedDepositProductRepository, final DepositProductDataValidator fromApiJsonDataValidator,
            final DepositProductAssembler depositProductAssembler,
            final ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService,
            final InterestRateChartAssembler chartAssembler) {
        this.context = context;
        this.fixedDepositProductRepository = fixedDepositProductRepository;
        this.fromApiJsonDataValidator = fromApiJsonDataValidator;
        this.depositProductAssembler = depositProductAssembler;
        this.logger = LoggerFactory.getLogger(FixedDepositProductWritePlatformServiceJpaRepositoryImpl.class);
        this.accountMappingWritePlatformService = accountMappingWritePlatformService;
        this.chartAssembler = chartAssembler;
    }

    @Transactional
    @Override
    public CommandProcessingResult create(final JsonCommand command) {

        try {
            this.fromApiJsonDataValidator.validateForFixedDepositCreate(command.json());

            final FixedDepositProduct product = this.depositProductAssembler.assembleFixedDepositProduct(command);

            this.fixedDepositProductRepository.save(product);

            // save accounting mappings
            this.accountMappingWritePlatformService.createSavingProductToGLAccountMapping(product.getId(), command,
                    DepositAccountType.FIXED_DEPOSIT);

            return new CommandProcessingResultBuilder() //
                    .withEntityId(product.getId()) //
                    .build();
        } catch (final DataAccessException e) {
            handleDataIntegrityIssues(command, e);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult update(final Long productId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            this.fromApiJsonDataValidator.validateForFixedDepositUpdate(command.json());

            final FixedDepositProduct product = this.fixedDepositProductRepository.findOne(productId);
            if (product == null) { throw new FixedDepositProductNotFoundException(productId); }
            product.setHelpers(this.chartAssembler);

            final Map<String, Object> changes = product.update(command);

            if (changes.containsKey(chargesParamName)) {
                final Set<Charge> savingsProductCharges = this.depositProductAssembler.assembleListOfSavingsProductCharges(command, product
                        .currency().getCode());
                final boolean updated = product.update(savingsProductCharges);
                if (!updated) {
                    changes.remove(chargesParamName);
                }
            }

            // accounting related changes
            final boolean accountingTypeChanged = changes.containsKey(accountingRuleParamName);
            final Map<String, Object> accountingMappingChanges = this.accountMappingWritePlatformService
                    .updateSavingsProductToGLAccountMapping(product.getId(), command, accountingTypeChanged, product.getAccountingType(),
                            DepositAccountType.FIXED_DEPOSIT);
            changes.putAll(accountingMappingChanges);

            if (!changes.isEmpty()) {
                this.fixedDepositProductRepository.save(product);
            }

            return new CommandProcessingResultBuilder() //
                    .withEntityId(product.getId()) //
                    .with(changes).build();
        } catch (final DataAccessException e) {
            handleDataIntegrityIssues(command, e);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult delete(final Long productId) {

        this.context.authenticatedUser();
        final FixedDepositProduct product = this.fixedDepositProductRepository.findOne(productId);
        if (product == null) { throw new FixedDepositProductNotFoundException(productId); }

        this.fixedDepositProductRepository.delete(product);

        return new CommandProcessingResultBuilder() //
                .withEntityId(product.getId()) //
                .build();
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataAccessException dae) {

        final Throwable realCause = dae.getMostSpecificCause();
        if (realCause.getMessage().contains("sp_unq_name")) {

            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.product.savings.duplicate.name", "Savings product with name `" + name
                    + "` already exists", "name", name);
        } else if (realCause.getMessage().contains("sp_unq_short_name")) {

            final String shortName = command.stringValueOfParameterNamed("shortName");
            throw new PlatformDataIntegrityException("error.msg.product.savings.duplicate.short.name", "Savings product with short name `"
                    + shortName + "` already exists", "shortName", shortName);
        }

        logAsErrorUnexpectedDataIntegrityException(dae);
        throw new PlatformDataIntegrityException("error.msg.savingsproduct.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataAccessException dae) {
        this.logger.error(dae.getMessage(), dae);
    }
}