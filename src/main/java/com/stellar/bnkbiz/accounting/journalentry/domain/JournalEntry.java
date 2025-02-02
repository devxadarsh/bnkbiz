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
package com.stellar.bnkbiz.accounting.journalentry.domain;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;
import com.stellar.bnkbiz.accounting.glaccount.domain.GLAccount;
import com.stellar.bnkbiz.infrastructure.core.domain.AbstractAuditableCustom;
import com.stellar.bnkbiz.organisation.office.domain.Office;
import com.stellar.bnkbiz.portfolio.client.domain.ClientTransaction;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.LoanTransaction;
import com.stellar.bnkbiz.portfolio.paymentdetail.domain.PaymentDetail;
import com.stellar.bnkbiz.portfolio.savings.domain.SavingsAccountTransaction;
import com.stellar.bnkbiz.useradministration.domain.AppUser;

@Entity
@Table(name = "acc_gl_journal_entry")
public class JournalEntry extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne(optional = true)
    @JoinColumn(name = "payment_details_id", nullable = true)
    private PaymentDetail paymentDetail;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private GLAccount glAccount;

    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_id")
    private JournalEntry reversalJournalEntry;

    @Column(name = "transaction_id", nullable = false, length = 50)
    private String transactionId;

    @ManyToOne
    @JoinColumn(name = "loan_transaction_id", nullable = false)
    private LoanTransaction loanTransaction;

    @ManyToOne
    @JoinColumn(name = "savings_transaction_id", nullable = false)
    private SavingsAccountTransaction savingsTransaction;

    @ManyToOne
    @JoinColumn(name = "client_transaction_id", nullable = false)
    private ClientTransaction clientTransaction;

    @Column(name = "reversed", nullable = false)
    private boolean reversed = false;

    @Column(name = "manual_entry", nullable = false)
    private boolean manualEntry = false;

    @Column(name = "entry_date")
    @Temporal(TemporalType.DATE)
    private Date transactionDate;

    @Column(name = "type_enum", nullable = false)
    private Integer type;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "entity_type_enum", length = 50)
    private Integer entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "ref_num")
    private String referenceNumber;

    public static JournalEntry createNew(final Office office, final PaymentDetail paymentDetail, final GLAccount glAccount,
            final String currencyCode, final String transactionId, final boolean manualEntry, final Date transactionDate,
            final JournalEntryType journalEntryType, final BigDecimal amount, final String description, final Integer entityType,
            final Long entityId, final String referenceNumber, final LoanTransaction loanTransaction,
            final SavingsAccountTransaction savingsTransaction, final ClientTransaction clientTransaction) {
        return new JournalEntry(office, paymentDetail, glAccount, currencyCode, transactionId, manualEntry, transactionDate,
                journalEntryType.getValue(), amount, description, entityType, entityId, referenceNumber, loanTransaction,
                savingsTransaction, clientTransaction);
    }

    protected JournalEntry() {
        //
    }

    public JournalEntry(final Office office, final PaymentDetail paymentDetail, final GLAccount glAccount, final String currencyCode,
            final String transactionId, final boolean manualEntry, final Date transactionDate, final Integer type, final BigDecimal amount,
            final String description, final Integer entityType, final Long entityId, final String referenceNumber,
            final LoanTransaction loanTransaction, final SavingsAccountTransaction savingsTransaction,
            final ClientTransaction clientTransaction) {
        this.office = office;
        this.glAccount = glAccount;
        this.reversalJournalEntry = null;
        this.transactionId = transactionId;
        this.reversed = false;
        this.manualEntry = manualEntry;
        this.transactionDate = transactionDate;
        this.type = type;
        this.amount = amount;
        this.description = StringUtils.defaultIfEmpty(description, null);
        this.entityType = entityType;
        this.entityId = entityId;
        this.referenceNumber = referenceNumber;
        this.currencyCode = currencyCode;
        this.loanTransaction = loanTransaction;
        this.savingsTransaction = savingsTransaction;
        this.clientTransaction = clientTransaction;
        this.paymentDetail = paymentDetail;
    }

    public boolean isDebitEntry() {
        return JournalEntryType.DEBIT.getValue().equals(this.type);
    }

    public Integer getType() {
        return this.type;
    }

    public Office getOffice() {
        return this.office;
    }

    public GLAccount getGlAccount() {
        return this.glAccount;
    }

    public Date getTransactionDate() {
        return this.transactionDate;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setReversalJournalEntry(final JournalEntry reversalJournalEntry) {
        this.reversalJournalEntry = reversalJournalEntry;
    }

    public void setReversed(final boolean reversed) {
        this.reversed = reversed;
    }

    public String getReferenceNumber() {
        return this.referenceNumber;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public LoanTransaction getLoanTransaction() {
        return this.loanTransaction;
    }

    public SavingsAccountTransaction getSavingsTransaction() {
        return this.savingsTransaction;
    }

    public PaymentDetail getPaymentDetails() {
        return this.paymentDetail;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public ClientTransaction getClientTransaction() {
        return this.clientTransaction;
    }

    public Long getEntityId() {
        return this.entityId ;
    }
    
    public Integer getEntityType() {
        return this.entityType ;
    }
    
}