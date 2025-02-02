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
package com.stellar.bnkbiz.organisation.office.service;

import java.util.Map;

import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResultBuilder;
import com.stellar.bnkbiz.infrastructure.core.exception.PlatformDataIntegrityException;
import com.stellar.bnkbiz.infrastructure.security.exception.NoAuthorizationException;
import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
import com.stellar.bnkbiz.organisation.monetary.domain.ApplicationCurrency;
import com.stellar.bnkbiz.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import com.stellar.bnkbiz.organisation.monetary.domain.MonetaryCurrency;
import com.stellar.bnkbiz.organisation.monetary.domain.Money;
import com.stellar.bnkbiz.organisation.office.domain.Office;
import com.stellar.bnkbiz.organisation.office.domain.OfficeRepository;
import com.stellar.bnkbiz.organisation.office.domain.OfficeTransaction;
import com.stellar.bnkbiz.organisation.office.domain.OfficeTransactionRepository;
import com.stellar.bnkbiz.organisation.office.exception.OfficeNotFoundException;
import com.stellar.bnkbiz.organisation.office.serialization.OfficeCommandFromApiJsonDeserializer;
import com.stellar.bnkbiz.organisation.office.serialization.OfficeTransactionCommandFromApiJsonDeserializer;
import com.stellar.bnkbiz.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OfficeWritePlatformServiceJpaRepositoryImpl implements OfficeWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(OfficeWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final OfficeCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final OfficeTransactionCommandFromApiJsonDeserializer moneyTransferCommandFromApiJsonDeserializer;
    private final OfficeRepository officeRepository;
    private final OfficeTransactionRepository officeTransactionRepository;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;

    @Autowired
    public OfficeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final OfficeCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final OfficeTransactionCommandFromApiJsonDeserializer moneyTransferCommandFromApiJsonDeserializer,
            final OfficeRepository officeRepository, final OfficeTransactionRepository officeMonetaryTransferRepository,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.moneyTransferCommandFromApiJsonDeserializer = moneyTransferCommandFromApiJsonDeserializer;
        this.officeRepository = officeRepository;
        this.officeTransactionRepository = officeMonetaryTransferRepository;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
    }

    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(value = "offices", key = "T(com.stellar.bnkbiz.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'of')"),
            @CacheEvict(value = "officesForDropdown", key = "T(com.stellar.bnkbiz.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'ofd')") })
    public CommandProcessingResult createOffice(final JsonCommand command) {

        try {
            final AppUser currentUser = this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            Long parentId = null;
            if (command.parameterExists("parentId")) {
                parentId = command.longValueOfParameterNamed("parentId");
            }

            final Office parent = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, parentId);
            final Office office = Office.fromJson(parent, command);

            // pre save to generate id for use in office hierarchy
            this.officeRepository.save(office);

            office.generateHierarchy();

            this.officeRepository.save(office);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(office.getId()) //
                    .withOfficeId(office.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleOfficeDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(value = "offices", key = "T(com.stellar.bnkbiz.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'of')"),
            @CacheEvict(value = "officesForDropdown", key = "T(com.stellar.bnkbiz.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'ofd')"),
            @CacheEvict(value = "officesById", key = "T(com.stellar.bnkbiz.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#officeId)") })
    public CommandProcessingResult updateOffice(final Long officeId, final JsonCommand command) {

        try {
            final AppUser currentUser = this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            Long parentId = null;
            if (command.parameterExists("parentId")) {
                parentId = command.longValueOfParameterNamed("parentId");
            }

            final Office office = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, officeId);

            final Map<String, Object> changes = office.update(command);

            if (changes.containsKey("parentId")) {
                final Office parent = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, parentId);
                office.update(parent);
            }

            if (!changes.isEmpty()) {
                this.officeRepository.saveAndFlush(office);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(office.getId()) //
                    .withOfficeId(office.getId()) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleOfficeDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult officeTransaction(final JsonCommand command) {

        this.context.authenticatedUser();

        this.moneyTransferCommandFromApiJsonDeserializer.validateOfficeTransfer(command.json());

        Long officeId = null;
        Office fromOffice = null;
        final Long fromOfficeId = command.longValueOfParameterNamed("fromOfficeId");
        if (fromOfficeId != null) {
            fromOffice = this.officeRepository.findOne(fromOfficeId);
            officeId = fromOffice.getId();
        }
        Office toOffice = null;
        final Long toOfficeId = command.longValueOfParameterNamed("toOfficeId");
        if (toOfficeId != null) {
            toOffice = this.officeRepository.findOne(toOfficeId);
            officeId = toOffice.getId();
        }

        if (fromOffice == null && toOffice == null) { throw new OfficeNotFoundException(toOfficeId); }

        final String currencyCode = command.stringValueOfParameterNamed("currencyCode");
        final ApplicationCurrency appCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currencyCode);

        final MonetaryCurrency currency = new MonetaryCurrency(appCurrency.getCode(), appCurrency.getDecimalPlaces(),
                appCurrency.getCurrencyInMultiplesOf());
        final Money amount = Money.of(currency, command.bigDecimalValueOfParameterNamed("transactionAmount"));

        final OfficeTransaction entity = OfficeTransaction.fromJson(fromOffice, toOffice, amount, command);

        this.officeTransactionRepository.save(entity);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(entity.getId()) //
                .withOfficeId(officeId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteOfficeTransaction(final Long transactionId, final JsonCommand command) {

        this.context.authenticatedUser();

        this.officeTransactionRepository.delete(transactionId);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(transactionId) //
                .build();
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleOfficeDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("externalid_org")) {
            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.office.duplicate.externalId", "Office with externalId `" + externalId
                    + "` already exists", "externalId", externalId);
        } else if (realCause.getMessage().contains("name_org")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.office.duplicate.name", "Office with name `" + name + "` already exists",
                    "name", name);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.office.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    /*
     * used to restrict modifying operations to office that are either the users
     * office or lower (child) in the office hierarchy
     */
    private Office validateUserPriviledgeOnOfficeAndRetrieve(final AppUser currentUser, final Long officeId) {

        final Long userOfficeId = currentUser.getOffice().getId();
        final Office userOffice = this.officeRepository.findOne(userOfficeId);
        if (userOffice == null) { throw new OfficeNotFoundException(userOfficeId); }

        if (userOffice.doesNotHaveAnOfficeInHierarchyWithId(officeId)) { throw new NoAuthorizationException(
                "User does not have sufficient priviledges to act on the provided office."); }

        Office officeToReturn = userOffice;
        if (!userOffice.identifiedBy(officeId)) {
            officeToReturn = this.officeRepository.findOne(officeId);
            if (officeToReturn == null) { throw new OfficeNotFoundException(officeId); }
        }

        return officeToReturn;
    }

    public PlatformSecurityContext getContext() {
        return this.context;
    }
}