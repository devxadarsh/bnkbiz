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
package com.stellar.bnkbiz.organisation.teller.service;

import java.util.Map;
import java.util.Set;

import com.stellar.bnkbiz.accounting.common.AccountingConstants.FINANCIAL_ACTIVITY;
import com.stellar.bnkbiz.accounting.financialactivityaccount.domain.FinancialActivityAccount;
import com.stellar.bnkbiz.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import com.stellar.bnkbiz.accounting.glaccount.domain.GLAccount;
import com.stellar.bnkbiz.accounting.journalentry.domain.JournalEntry;
import com.stellar.bnkbiz.accounting.journalentry.domain.JournalEntryRepository;
import com.stellar.bnkbiz.accounting.journalentry.domain.JournalEntryType;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResultBuilder;
import com.stellar.bnkbiz.infrastructure.core.exception.PlatformDataIntegrityException;
import com.stellar.bnkbiz.infrastructure.security.exception.NoAuthorizationException;
import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
import com.stellar.bnkbiz.organisation.office.domain.Office;
import com.stellar.bnkbiz.organisation.office.domain.OfficeRepository;
import com.stellar.bnkbiz.organisation.office.exception.OfficeNotFoundException;
import com.stellar.bnkbiz.organisation.staff.domain.Staff;
import com.stellar.bnkbiz.organisation.staff.domain.StaffRepository;
import com.stellar.bnkbiz.organisation.staff.exception.StaffNotFoundException;
import com.stellar.bnkbiz.organisation.teller.domain.Cashier;
import com.stellar.bnkbiz.organisation.teller.domain.CashierRepository;
import com.stellar.bnkbiz.organisation.teller.domain.CashierTransaction;
import com.stellar.bnkbiz.organisation.teller.domain.CashierTransactionRepository;
import com.stellar.bnkbiz.organisation.teller.domain.CashierTxnType;
import com.stellar.bnkbiz.organisation.teller.domain.Teller;
import com.stellar.bnkbiz.organisation.teller.domain.TellerRepository;
import com.stellar.bnkbiz.organisation.teller.domain.TellerRepositoryWrapper;
import com.stellar.bnkbiz.organisation.teller.exception.CashierExistForTellerException;
import com.stellar.bnkbiz.organisation.teller.exception.CashierNotFoundException;
import com.stellar.bnkbiz.organisation.teller.exception.TellerNotFoundException;
import com.stellar.bnkbiz.organisation.teller.serialization.TellerCommandFromApiJsonDeserializer;
import com.stellar.bnkbiz.portfolio.client.domain.ClientTransaction;
import com.stellar.bnkbiz.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TellerWritePlatformServiceJpaImpl implements TellerWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(TellerWritePlatformServiceJpaImpl.class);

    private final PlatformSecurityContext context;
    private final TellerCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final TellerRepository tellerRepository;
    private final TellerRepositoryWrapper tellerRepositoryWrapper;
    private final OfficeRepository officeRepository;
    private final StaffRepository staffRepository;
    private final CashierRepository cashierRepository;
    private final CashierTransactionRepository cashierTxnRepository;
    private final JournalEntryRepository glJournalEntryRepository;
    private final FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper;

    @Autowired
    public TellerWritePlatformServiceJpaImpl(final PlatformSecurityContext context,
            final TellerCommandFromApiJsonDeserializer fromApiJsonDeserializer, final TellerRepository tellerRepository,
            final TellerRepositoryWrapper tellerRepositoryWrapper, final OfficeRepository officeRepository,
            final StaffRepository staffRepository, CashierRepository cashierRepository, CashierTransactionRepository cashierTxnRepository,
            JournalEntryRepository glJournalEntryRepository,
            FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.tellerRepository = tellerRepository;
        this.tellerRepositoryWrapper = tellerRepositoryWrapper;
        this.officeRepository = officeRepository;
        this.staffRepository = staffRepository;
        this.cashierRepository = cashierRepository;
        this.cashierTxnRepository = cashierTxnRepository;
        this.glJournalEntryRepository = glJournalEntryRepository;
        this.financialActivityAccountRepositoryWrapper = financialActivityAccountRepositoryWrapper;
    }

    @Override
    @Transactional
    public CommandProcessingResult createTeller(JsonCommand command) {
        try {
            this.context.authenticatedUser();

            final Long officeId = command.longValueOfParameterNamed("officeId");

            this.fromApiJsonDeserializer.validateForCreateAndUpdateTeller(command.json());

            // final Office parent =
            // validateUserPriviledgeOnOfficeAndRetrieve(currentUser, officeId);
            final Office tellerOffice = this.officeRepository.findOne(officeId);
            if (tellerOffice == null) { throw new OfficeNotFoundException(officeId); }

            final Teller teller = Teller.fromJson(tellerOffice, command);

            // pre save to generate id for use in office hierarchy
            this.tellerRepository.save(teller);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(teller.getId()) //
                    .withOfficeId(teller.getOffice().getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult modifyTeller(Long tellerId, JsonCommand command) {
        try {

            final Long officeId = command.longValueOfParameterNamed("officeId");
            final Office tellerOffice = this.officeRepository.findOne(officeId);
            if (tellerOffice == null) { throw new OfficeNotFoundException(officeId); }

            final AppUser currentUser = this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreateAndUpdateTeller(command.json());

            final Teller teller = validateUserPriviledgeOnTellerAndRetrieve(currentUser, tellerId);

            final Map<String, Object> changes = teller.update(tellerOffice, command);

            if (!changes.isEmpty()) {
                this.tellerRepository.saveAndFlush(teller);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(teller.getId()) //
                    .withOfficeId(teller.officeId()) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    /*
     * used to restrict modifying operations to office that are either the users
     * office or lower (child) in the office hierarchy
     */
    private Teller validateUserPriviledgeOnTellerAndRetrieve(final AppUser currentUser, final Long tellerId) {

        final Long userOfficeId = currentUser.getOffice().getId();
        final Office userOffice = this.officeRepository.findOne(userOfficeId);
        if (userOffice == null) { throw new OfficeNotFoundException(userOfficeId); }

        final Teller tellerToReturn = this.tellerRepository.findOne(tellerId);
        if (tellerToReturn != null) {
            final Long tellerOfficeId = tellerToReturn.officeId();
            if (userOffice.doesNotHaveAnOfficeInHierarchyWithId(tellerOfficeId)) { throw new NoAuthorizationException(
                    "User does not have sufficient priviledges to act on the provided office."); }
        } else {
            throw new TellerNotFoundException(tellerId);
        }

        return tellerToReturn;
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteTeller(Long tellerId) {
        // TODO Auto-generated method stub

        Teller teller = tellerRepositoryWrapper.findOneWithNotFoundDetection(tellerId);
        Set<Cashier> isTellerIdPresentInCashier = teller.getCashiers();

        for (final Cashier tellerIdInCashier : isTellerIdPresentInCashier) {
            if (tellerIdInCashier.getTeller().getId().toString()
                    .equalsIgnoreCase(tellerId.toString())) { throw new CashierExistForTellerException(tellerId); }

        }
        tellerRepository.delete(teller);
        return new CommandProcessingResultBuilder() //
                .withEntityId(teller.getId()) //
                .build();

    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleTellerDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("m_tellers_name_unq")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.teller.duplicate.name", "Teller with name `" + name + "` already exists",
                    "name", name);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.teller.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    @Override
    public CommandProcessingResult allocateCashierToTeller(final Long tellerId, JsonCommand command) {
        try {
            this.context.authenticatedUser();
            Long hourStartTime;
            Long minStartTime;
            Long hourEndTime;
            Long minEndTime;
            String startTime = " ";
            String endTime = " ";
            final Teller teller = this.tellerRepository.findOne(tellerId);
            if (teller == null) { throw new TellerNotFoundException(tellerId); }
            final Office tellerOffice = teller.getOffice();

            final Long staffId = command.longValueOfParameterNamed("staffId");

            this.fromApiJsonDeserializer.validateForAllocateCashier(command.json());

            final Staff staff = this.staffRepository.findOne(staffId);
            if (staff == null) { throw new StaffNotFoundException(staffId); }
            final Boolean isFullDay = command.booleanObjectValueOfParameterNamed("isFullDay");
            if (!isFullDay) {
                hourStartTime = command.longValueOfParameterNamed("hourStartTime");
                minStartTime = command.longValueOfParameterNamed("minStartTime");

                if (minStartTime == 0)
                    startTime = hourStartTime.toString() + ":" + minStartTime.toString() + "0";
                else
                    startTime = hourStartTime.toString() + ":" + minStartTime.toString();

                hourEndTime = command.longValueOfParameterNamed("hourEndTime");
                minEndTime = command.longValueOfParameterNamed("minEndTime");
                if (minEndTime == 0)
                    endTime = hourEndTime.toString() + ":" + minEndTime.toString() + "0";
                else
                    endTime = hourEndTime.toString() + ":" + minEndTime.toString();

            }

            final Cashier cashier = Cashier.fromJson(tellerOffice, teller, staff, startTime, endTime, command);

            this.cashierRepository.save(cashier);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(teller.getId()) //
                    .withSubEntityId(cashier.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult updateCashierAllocation(Long tellerId, Long cashierId, JsonCommand command) {
        try {
            final AppUser currentUser = this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForAllocateCashier(command.json());

            final Long staffId = command.longValueOfParameterNamed("staffId");
            final Staff staff = this.staffRepository.findOne(staffId);
            if (staff == null) { throw new StaffNotFoundException(staffId); }

            final Cashier cashier = validateUserPriviledgeOnCashierAndRetrieve(currentUser, tellerId, cashierId);

            cashier.setStaff(staff);

            // TODO - check if staff office and teller office match

            final Map<String, Object> changes = cashier.update(command);

            if (!changes.isEmpty()) {
                this.cashierRepository.saveAndFlush(cashier);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(cashier.getTeller().getId()) //
                    .withSubEntityId(cashier.getId()) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    private Cashier validateUserPriviledgeOnCashierAndRetrieve(final AppUser currentUser, final Long tellerId, final Long cashierId) {

        validateUserPriviledgeOnTellerAndRetrieve(currentUser, tellerId);

        final Cashier cashierToReturn = this.cashierRepository.findOne(cashierId);

        return cashierToReturn;
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteCashierAllocation(Long tellerId, Long cashierId, JsonCommand command) {
        try {
            final AppUser currentUser = this.context.authenticatedUser();
            final Cashier cashier = validateUserPriviledgeOnCashierAndRetrieve(currentUser, tellerId, cashierId);
            this.cashierRepository.delete(cashier);

        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }

        return new CommandProcessingResultBuilder() //
                .withEntityId(cashierId) //
                .build();
    }

    /*
     * @Override public CommandProcessingResult inwardCashToCashier (final Long
     * cashierId, final CashierTransaction cashierTxn) { CashierTxnType txnType
     * = CashierTxnType.INWARD_CASH_TXN; // pre save to generate id for use in
     * office hierarchy this.cashierTxnRepository.save(cashierTxn); }
     */

    @Override
    public CommandProcessingResult allocateCashToCashier(final Long cashierId, JsonCommand command) {
        return doTransactionForCashier(cashierId, CashierTxnType.ALLOCATE, command); // For
                                                                                     // fund
                                                                                     // allocation
                                                                                     // to
                                                                                     // cashier
    }

    @Override
    public CommandProcessingResult settleCashFromCashier(final Long cashierId, JsonCommand command) {
        return doTransactionForCashier(cashierId, CashierTxnType.SETTLE, command); // For
                                                                                   // fund
                                                                                   // settlement
                                                                                   // from
                                                                                   // cashier
    }

    private CommandProcessingResult doTransactionForCashier(final Long cashierId, final CashierTxnType txnType, JsonCommand command) {
        try {
            final AppUser currentUser = this.context.authenticatedUser();

            final Cashier cashier = this.cashierRepository.findOne(cashierId);
            if (cashier == null) { throw new CashierNotFoundException(cashierId); }

            this.fromApiJsonDeserializer.validateForCashTxnForCashier(command.json());

            final String entityType = command.stringValueOfParameterNamed("entityType");
            final Long entityId = command.longValueOfParameterNamed("entityId");
            if (entityType != null) {
                if (entityType.equals("loan account")) {
                    // TODO : Check if loan account exists
                    // LoanAccount loan = null;
                    // if (loan == null) { throw new
                    // LoanAccountFoundException(entityId); }
                } else if (entityType.equals("savings account")) {
                    // TODO : Check if loan account exists
                    // SavingsAccount savingsaccount = null;
                    // if (savingsaccount == null) { throw new
                    // SavingsAccountNotFoundException(entityId); }

                }
                if (entityType.equals("client")) {
                    // TODO: Check if client exists
                    // Client client = null;
                    // if (client == null) { throw new
                    // ClientNotFoundException(entityId); }
                } else {
                    // TODO : Invalid type handling
                }
            }

            final CashierTransaction cashierTxn = CashierTransaction.fromJson(cashier, command);
            cashierTxn.setTxnType(txnType.getId());

            this.cashierTxnRepository.save(cashierTxn);

            // Pass the journal entries
            FinancialActivityAccount mainVaultFinancialActivityAccount = this.financialActivityAccountRepositoryWrapper
                    .findByFinancialActivityTypeWithNotFoundDetection(FINANCIAL_ACTIVITY.CASH_AT_MAINVAULT.getValue());
            FinancialActivityAccount tellerCashFinancialActivityAccount = this.financialActivityAccountRepositoryWrapper
                    .findByFinancialActivityTypeWithNotFoundDetection(FINANCIAL_ACTIVITY.CASH_AT_TELLER.getValue());
            GLAccount creditAccount = null;
            GLAccount debitAccount = null;
            if (txnType.equals(CashierTxnType.ALLOCATE)) {
                debitAccount = tellerCashFinancialActivityAccount.getGlAccount();
                creditAccount = mainVaultFinancialActivityAccount.getGlAccount();
            } else if (txnType.equals(CashierTxnType.SETTLE)) {
                debitAccount = mainVaultFinancialActivityAccount.getGlAccount();
                creditAccount = tellerCashFinancialActivityAccount.getGlAccount();
            }

            final Office cashierOffice = cashier.getTeller().getOffice();

            final Long time = System.currentTimeMillis();
            final String uniqueVal = String.valueOf(time) + currentUser.getId() + cashierOffice.getId();
            final String transactionId = Long.toHexString(Long.parseLong(uniqueVal));
            ClientTransaction clientTransaction = null;

            final JournalEntry debitJournalEntry = JournalEntry.createNew(cashierOffice, null, // payment
                                                                                               // detail
                    debitAccount, "USD", // FIXME: Take currency code from the
                                         // transaction
                    transactionId, false, // manual entry
                    cashierTxn.getTxnDate(), JournalEntryType.DEBIT, cashierTxn.getTxnAmount(), cashierTxn.getTxnNote(), // Description
                    null, null, null, // entity Type, entityId, reference number
                    null, null, clientTransaction); // Loan and Savings Txn

            final JournalEntry creditJournalEntry = JournalEntry.createNew(cashierOffice, null, // payment
                                                                                                // detail
                    creditAccount, "USD", // FIXME: Take currency code from the
                                          // transaction
                    transactionId, false, // manual entry
                    cashierTxn.getTxnDate(), JournalEntryType.CREDIT, cashierTxn.getTxnAmount(), cashierTxn.getTxnNote(), // Description
                    null, null, null, // entity Type, entityId, reference number
                    null, null, clientTransaction); // Loan and Savings Txn

            this.glJournalEntryRepository.saveAndFlush(debitJournalEntry);
            this.glJournalEntryRepository.saveAndFlush(creditJournalEntry);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(cashier.getId()) //
                    .withSubEntityId(cashierTxn.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

}
