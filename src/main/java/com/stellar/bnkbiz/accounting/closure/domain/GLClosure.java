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
package com.stellar.bnkbiz.accounting.closure.domain;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

//import org.apache.commons.lang3.StringUtils;
import com.stellar.bnkbiz.accounting.closure.api.GLClosureJsonInputParams;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.domain.AbstractAuditableCustom;
import com.stellar.bnkbiz.organisation.office.domain.Office;
import com.stellar.bnkbiz.useradministration.domain.AppUser;
import jakarta.persistence.Entity;
import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "acc_gl_closure", uniqueConstraints = { @UniqueConstraint(columnNames = { "office_id", "closing_date" }, name = "office_id_closing_date") })
public class GLClosure extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = true;

    @Column(name = "closing_date")
    @Temporal(TemporalType.DATE)
    private Date closingDate;

    @Column(name = "comments", nullable = true, length = 500)
    private String comments;

    protected GLClosure() {
        //
    }

    public GLClosure(final Office office, final Date closingDate, final String comments) {
        this.office = office;
        this.deleted = false;
        this.closingDate = closingDate;
        this.comments = StringUtils.defaultIfEmpty(comments, null);
        if (this.comments != null) {
            this.comments = this.comments.trim();
        }
    }

    public static GLClosure fromJson(final Office office, final JsonCommand command) {
        final Date closingDate = command.DateValueOfParameterNamed(GLClosureJsonInputParams.CLOSING_DATE.getValue());
        final String comments = command.stringValueOfParameterNamed(GLClosureJsonInputParams.COMMENTS.getValue());
        return new GLClosure(office, closingDate, comments);
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(5);
        handlePropertyUpdate(command, actualChanges, GLClosureJsonInputParams.COMMENTS.getValue(), this.comments);
        return actualChanges;
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            final String propertyToBeUpdated) {
        if (command.isChangeInStringParameterNamed(paramName, propertyToBeUpdated)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            // now update actual property
            if (paramName.equals(GLClosureJsonInputParams.COMMENTS.getValue())) {
                this.comments = newValue;
            }
        }
    }

    public Date getClosingDate() {
        return this.closingDate;
    }

    public Office getOffice() {
        return this.office;
    }

}