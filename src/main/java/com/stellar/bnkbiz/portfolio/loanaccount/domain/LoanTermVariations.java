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
package com.stellar.bnkbiz.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import com.stellar.bnkbiz.infrastructure.core.data.EnumOptionData;
import com.stellar.bnkbiz.portfolio.loanaccount.data.LoanTermVariationsData;
import com.stellar.bnkbiz.portfolio.loanproduct.service.LoanEnumerations;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_loan_term_variations")
public class LoanTermVariations extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "term_type", nullable = false)
    private Integer termType;

    @Temporal(TemporalType.DATE)
    @Column(name = "applicable_date", nullable = false)
    private Date termApplicableFrom;

    @Column(name = "decimal_value", scale = 6, precision = 19)
    private BigDecimal decimalValue;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_value")
    private Date dateValue;

    @Column(name = "is_specific_to_installment", nullable = false)
    private boolean isSpecificToInstallment;

    @Column(name = "applied_on_loan_status", nullable = false)
    private Integer onLoanStatus;

    public LoanTermVariations(final Integer termType, final Date termApplicableFrom, final BigDecimal decimalValue, final Date dateValue,
            final boolean isSpecificToInstallment, final Loan loan) {
        this.loan = loan;
        this.termApplicableFrom = termApplicableFrom;
        this.termType = termType;
        this.decimalValue = decimalValue;
        this.dateValue = dateValue;
        this.isSpecificToInstallment = isSpecificToInstallment;
        this.onLoanStatus = loan.status().getValue();
    }
    
    public LoanTermVariations(final Integer termType, final Date termApplicableFrom, final BigDecimal decimalValue, final Date dateValue,
            final boolean isSpecificToInstallment, final Loan loan, final Integer loanStatus) {
        this.loan = loan;
        this.termApplicableFrom = termApplicableFrom;
        this.termType = termType;
        this.decimalValue = decimalValue;
        this.dateValue = dateValue;
        this.isSpecificToInstallment = isSpecificToInstallment;
        this.onLoanStatus = loanStatus;
    }

    protected LoanTermVariations() {

    }

    public LoanTermVariationType getTermType() {
        return LoanTermVariationType.fromInt(this.termType);
    }

    public LoanTermVariationsData toData() {
        LocalDate termStartDate = new LocalDate(this.termApplicableFrom);
        LocalDate dateValue = null;
        if (this.dateValue != null) {
            dateValue = new LocalDate(this.dateValue);
        }
        EnumOptionData type = LoanEnumerations.loanvariationType(this.termType);
        return new LoanTermVariationsData(getId(), type, termStartDate, this.decimalValue, dateValue, this.isSpecificToInstallment);
    }

    public Date getTermApplicableFrom() {
        return this.termApplicableFrom;
    }

    public LocalDate fetchTermApplicaDate() {
        return new LocalDate(this.termApplicableFrom);
    }

    public BigDecimal getTermValue() {
        return this.decimalValue;
    }

    public Date getDateValue() {
        return this.dateValue;
    }

    public LocalDate fetchDateValue() {
        return this.dateValue == null ? null : new LocalDate(this.dateValue);
    }

    public void setTermApplicableFrom(Date termApplicableFrom) {
        this.termApplicableFrom = termApplicableFrom;
    }

    public void setDecimalValue(BigDecimal decimalValue) {
        this.decimalValue = decimalValue;
    }

    
    public Integer getOnLoanStatus() {
        return this.onLoanStatus;
    }

}
