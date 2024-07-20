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
package com.stellar.bnkbiz.infrastructure.sms.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.sms.SmsApiConstants;
import com.stellar.bnkbiz.organisation.staff.domain.Staff;
import com.stellar.bnkbiz.portfolio.client.domain.Client;
import com.stellar.bnkbiz.portfolio.group.domain.Group;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "sms_messages_outbound")
public class SmsMessage extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = true)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = true)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = true)
    private Staff staff;

    @Column(name = "status_enum", nullable = false)
    private Integer statusType;

    @Column(name = "mobile_no", nullable = false, length = 50)
    private String mobileNo;

    @Column(name = "message", nullable = false)
    private String message;

    public static SmsMessage pendingSms(final Group group, final Client client, final Staff staff, final String message,
            final String mobileNo) {
        return new SmsMessage(group, client, staff, SmsMessageStatusType.PENDING, message, mobileNo);
    }

    protected SmsMessage() {
        //
    }

    private SmsMessage(final Group group, final Client client, final Staff staff, final SmsMessageStatusType statusType,
            final String message, final String mobileNo) {
        this.group = group;
        this.client = client;
        this.staff = staff;
        this.statusType = statusType.getValue();
        this.mobileNo = mobileNo;
        this.message = message;
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(1);

        if (command.isChangeInStringParameterNamed(SmsApiConstants.messageParamName, this.message)) {
            final String newValue = command.stringValueOfParameterNamed(SmsApiConstants.messageParamName);
            actualChanges.put(SmsApiConstants.messageParamName, newValue);
            this.message = StringUtils.defaultIfEmpty(newValue, null);
        }

        return actualChanges;
    }
}