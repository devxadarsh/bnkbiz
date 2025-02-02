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
package com.stellar.bnkbiz.accounting.provisioning.service;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.stellar.bnkbiz.accounting.glaccount.domain.GLAccount;
import com.stellar.bnkbiz.accounting.glaccount.domain.GLAccountRepository;
import com.stellar.bnkbiz.accounting.journalentry.service.JournalEntryWritePlatformService;
import com.stellar.bnkbiz.accounting.producttoaccountmapping.domain.PortfolioProductType;
import com.stellar.bnkbiz.accounting.provisioning.data.LoanProductProvisioningEntryData;
import com.stellar.bnkbiz.accounting.provisioning.data.ProvisioningEntryData;
import com.stellar.bnkbiz.accounting.provisioning.domain.LoanProductProvisioningEntry;
import com.stellar.bnkbiz.accounting.provisioning.domain.ProvisioningEntry;
import com.stellar.bnkbiz.accounting.provisioning.domain.ProvisioningEntryRepository;
import com.stellar.bnkbiz.accounting.provisioning.exception.NoProvisioningCriteriaDefinitionFound;
import com.stellar.bnkbiz.accounting.provisioning.exception.ProvisioningEntryAlreadyCreatedException;
import com.stellar.bnkbiz.accounting.provisioning.exception.ProvisioningEntryNotfoundException;
import com.stellar.bnkbiz.accounting.provisioning.exception.ProvisioningJournalEntriesCannotbeCreatedException;
import com.stellar.bnkbiz.accounting.provisioning.serialization.ProvisioningEntriesDefinitionJsonDeserializer;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResultBuilder;
import com.stellar.bnkbiz.infrastructure.core.serialization.FromJsonHelper;
import com.stellar.bnkbiz.infrastructure.core.service.DateUtils;
import com.stellar.bnkbiz.infrastructure.jobs.annotation.CronTarget;
import com.stellar.bnkbiz.infrastructure.jobs.service.JobName;
import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
import com.stellar.bnkbiz.organisation.monetary.domain.MonetaryCurrency;
import com.stellar.bnkbiz.organisation.monetary.domain.Money;
import com.stellar.bnkbiz.organisation.monetary.domain.MoneyHelper;
import com.stellar.bnkbiz.organisation.office.domain.Office;
import com.stellar.bnkbiz.organisation.office.domain.OfficeRepository;
import com.stellar.bnkbiz.organisation.provisioning.data.ProvisioningCriteriaData;
import com.stellar.bnkbiz.organisation.provisioning.domain.ProvisioningCategory;
import com.stellar.bnkbiz.organisation.provisioning.domain.ProvisioningCategoryRepository;
import com.stellar.bnkbiz.organisation.provisioning.service.ProvisioningCriteriaReadPlatformService;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.LoanProduct;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.LoanProductRepository;
import com.stellar.bnkbiz.useradministration.domain.AppUser;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

@Service
public class ProvisioningEntriesWritePlatformServiceJpaRepositoryImpl implements ProvisioningEntriesWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ProvisioningEntriesWritePlatformServiceJpaRepositoryImpl.class);

    private final ProvisioningEntriesReadPlatformService provisioningEntriesReadPlatformService;
    private final ProvisioningCriteriaReadPlatformService provisioningCriteriaReadPlatformService ;
    private final LoanProductRepository loanProductRepository;
    private final GLAccountRepository glAccountRepository;
    private final OfficeRepository officeRepository;
    private final ProvisioningCategoryRepository provisioningCategoryRepository;
    private final PlatformSecurityContext platformSecurityContext;
    private final ProvisioningEntryRepository provisioningEntryRepository;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final ProvisioningEntriesDefinitionJsonDeserializer fromApiJsonDeserializer;
    private final FromJsonHelper fromApiJsonHelper;
    
    @Autowired
    public ProvisioningEntriesWritePlatformServiceJpaRepositoryImpl(
            final ProvisioningEntriesReadPlatformService provisioningEntriesReadPlatformService,
            final ProvisioningCriteriaReadPlatformService provisioningCriteriaReadPlatformService,
            final LoanProductRepository loanProductRepository, final GLAccountRepository glAccountRepository,
            final OfficeRepository officeRepository, final ProvisioningCategoryRepository provisioningCategoryRepository,
            final PlatformSecurityContext platformSecurityContext, final ProvisioningEntryRepository provisioningEntryRepository,
            final JournalEntryWritePlatformService journalEntryWritePlatformService,
            final ProvisioningEntriesDefinitionJsonDeserializer fromApiJsonDeserializer, final FromJsonHelper fromApiJsonHelper) {
        this.provisioningEntriesReadPlatformService = provisioningEntriesReadPlatformService;
        this.provisioningCriteriaReadPlatformService = provisioningCriteriaReadPlatformService ;
        this.loanProductRepository = loanProductRepository;
        this.glAccountRepository = glAccountRepository;
        this.officeRepository = officeRepository;
        this.provisioningCategoryRepository = provisioningCategoryRepository;
        this.platformSecurityContext = platformSecurityContext;
        this.provisioningEntryRepository = provisioningEntryRepository;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public CommandProcessingResult createProvisioningJournalEntries(Long provisioningEntryId, JsonCommand command) {
        ProvisioningEntry requestedEntry = this.provisioningEntryRepository.findOne(provisioningEntryId);
        if (requestedEntry == null) { throw new ProvisioningEntryNotfoundException(provisioningEntryId); }

        ProvisioningEntryData exisProvisioningEntryData = this.provisioningEntriesReadPlatformService
                .retrieveExistingProvisioningIdDateWithJournals();
        revertAndAddJournalEntries(exisProvisioningEntryData, requestedEntry);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(requestedEntry.getId()).build();
    }

    private void revertAndAddJournalEntries(ProvisioningEntryData existingEntryData, ProvisioningEntry requestedEntry) {
        if (existingEntryData != null) {
            validateForCreateJournalEntry(existingEntryData, requestedEntry);
            this.journalEntryWritePlatformService.revertProvisioningJournalEntries(requestedEntry.getCreatedDate(),
                    existingEntryData.getId(), PortfolioProductType.PROVISIONING.getValue());
        }
        if(requestedEntry.getLoanProductProvisioningEntries() == null || requestedEntry.getLoanProductProvisioningEntries().size() == 0) {
            requestedEntry.setJournalEntryCreated(Boolean.FALSE);    
        }else {
            requestedEntry.setJournalEntryCreated(Boolean.TRUE);
        }
        
        this.provisioningEntryRepository.save(requestedEntry);
        this.journalEntryWritePlatformService.createProvisioningJournalEntries(requestedEntry);
    }

    private void validateForCreateJournalEntry(ProvisioningEntryData existingEntry, ProvisioningEntry requested) {
        Date existingDate = existingEntry.getCreatedDate();
        Date requestedDate = requested.getCreatedDate();
        if (existingDate.after(requestedDate) || existingDate.equals(requestedDate)) { throw new ProvisioningJournalEntriesCannotbeCreatedException(
                existingEntry.getCreatedDate(), requestedDate); }
    }

    private boolean isJournalEntriesRequired(JsonCommand command) {
        boolean bool = false;
        if (this.fromApiJsonHelper.parameterExists("createjournalentries", command.parsedJson())) {
            JsonObject jsonObject = command.parsedJson().getAsJsonObject();
            bool = jsonObject.get("createjournalentries").getAsBoolean();
        }
        return bool;
    }

    private Date parseDate(JsonCommand command) {
        LocalDate localDate = this.fromApiJsonHelper.extractLocalDateNamed("date", command.parsedJson());
        return localDate.toDate();
    }

    @Override
    @CronTarget(jobName = JobName.GENERATE_LOANLOSS_PROVISIONING)
    public void generateLoanLossProvisioningAmount() {
        Date currentDate  = DateUtils.getLocalDateOfTenant().toDate() ;
        boolean addJournalEntries = true;
        try {
            Collection<ProvisioningCriteriaData> criteriaCollection = this.provisioningCriteriaReadPlatformService.retrieveAllProvisioningCriterias() ; 
            if(criteriaCollection == null || criteriaCollection.size() == 0){
                return ;
                //FIXME: Do we need to throw NoProvisioningCriteriaDefinitionFound()?
            }
            createProvsioningEntry(currentDate, addJournalEntries);
        } catch (ProvisioningEntryAlreadyCreatedException peace) {} catch (DataIntegrityViolationException dive) {}
    }

    @Override
    public CommandProcessingResult createProvisioningEntries(JsonCommand command) {
        this.fromApiJsonDeserializer.validateForCreate(command.json());
        Date createdDate = parseDate(command);
        boolean addJournalEntries = isJournalEntriesRequired(command);
        try {
            Collection<ProvisioningCriteriaData> criteriaCollection = this.provisioningCriteriaReadPlatformService.retrieveAllProvisioningCriterias() ; 
            if(criteriaCollection == null || criteriaCollection.size() == 0){
                throw new NoProvisioningCriteriaDefinitionFound() ;
            }
            ProvisioningEntry requestedEntry = createProvsioningEntry(createdDate, addJournalEntries);
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(requestedEntry.getId()).build();
        } catch (DataIntegrityViolationException dve) {
            return CommandProcessingResult.empty();
        }
    }

    private ProvisioningEntry createProvsioningEntry(Date date, boolean addJournalEntries) {
        ProvisioningEntry existingEntry = this.provisioningEntryRepository.findByProvisioningEntryDate(date);
        if (existingEntry != null) { throw new ProvisioningEntryAlreadyCreatedException(existingEntry.getId(),
                existingEntry.getCreatedDate()); }
        AppUser currentUser = this.platformSecurityContext.authenticatedUser();
        AppUser lastModifiedBy = null;
        Date lastModifiedDate = null;
        Collection<LoanProductProvisioningEntry> nullEntries = null;
        ProvisioningEntry requestedEntry = new ProvisioningEntry(currentUser, date, lastModifiedBy, lastModifiedDate, nullEntries);
        Collection<LoanProductProvisioningEntry> entries = generateLoanProvisioningEntry(requestedEntry, date);
        requestedEntry.setProvisioningEntries(entries);
        if (addJournalEntries) {
            ProvisioningEntryData exisProvisioningEntryData = this.provisioningEntriesReadPlatformService
                    .retrieveExistingProvisioningIdDateWithJournals();
            revertAndAddJournalEntries(exisProvisioningEntryData, requestedEntry);
        } else {
            this.provisioningEntryRepository.save(requestedEntry);
        }
        return requestedEntry;
    }

    @Override
    public CommandProcessingResult reCreateProvisioningEntries(Long provisioningEntryId, JsonCommand command) {
        ProvisioningEntry requestedEntry = this.provisioningEntryRepository.findOne(provisioningEntryId);
        if (requestedEntry == null) { throw new ProvisioningEntryNotfoundException(provisioningEntryId); }
        requestedEntry.getLoanProductProvisioningEntries().clear();
        this.provisioningEntryRepository.save(requestedEntry);
        Collection<LoanProductProvisioningEntry> entries = generateLoanProvisioningEntry(requestedEntry, requestedEntry.getCreatedDate());
        requestedEntry.setProvisioningEntries(entries);
        this.provisioningEntryRepository.save(requestedEntry);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(requestedEntry.getId()).build();
    }

    private Collection<LoanProductProvisioningEntry> generateLoanProvisioningEntry(ProvisioningEntry parent, Date date) {
        Collection<LoanProductProvisioningEntryData> entries = this.provisioningEntriesReadPlatformService
                .retrieveLoanProductsProvisioningData(date);
        Map<LoanProductProvisioningEntry, LoanProductProvisioningEntry> provisioningEntries = new HashMap<>();
        for (LoanProductProvisioningEntryData data : entries) {
            LoanProduct loanProduct = this.loanProductRepository.findOne(data.getProductId());
            Office office = this.officeRepository.findOne(data.getOfficeId());
            ProvisioningCategory provisioningCategory = provisioningCategoryRepository.findOne(data.getCategoryId());
            GLAccount liabilityAccount = glAccountRepository.findOne(data.getLiablityAccount());
            GLAccount expenseAccount = glAccountRepository.findOne(data.getExpenseAccount());
            MonetaryCurrency currency = loanProduct.getPrincipalAmount().getCurrency();
            Money money = Money.of(currency, data.getOutstandingBalance());
            Money amountToReserve = money.percentageOf(data.getPercentage(), MoneyHelper.getRoundingMode());
            Long criteraId = data.getCriteriaId();
            LoanProductProvisioningEntry entry = new LoanProductProvisioningEntry(loanProduct, office, data.getCurrencyCode(),
                    provisioningCategory, data.getOverdueInDays(), amountToReserve.getAmount(), liabilityAccount, expenseAccount, criteraId);
            entry.setProvisioningEntry(parent);
            if (!provisioningEntries.containsKey(entry)) {
                provisioningEntries.put(entry, entry);
            } else {
                LoanProductProvisioningEntry entry1 = provisioningEntries.get(entry);
                entry1.addReservedAmount(entry.getReservedAmount());
            }
        }
        return provisioningEntries.values();
    }
}
