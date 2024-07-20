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
package com.stellar.bnkbiz.portfolio.accounts.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.stellar.bnkbiz.portfolio.charge.domain.Charge;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_shareaccounts_charges")
public class ShareAccountCharge extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private ShareAccount shareAccount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "charge_id", referencedColumnName = "id", nullable = false)
    private Charge charge;

    protected ShareAccountCharge() {

    }

    public ShareAccountCharge(final ShareAccount shareAccount, final Charge charge) {
        this.shareAccount = shareAccount;
        this.charge = charge;
    }

    public ShareAccountCharge(final Charge charge) {
        this.charge = charge;
    }

    public void setShareAccount(ShareAccount shareAccount) {
        this.shareAccount = shareAccount;
    }
    
    public Long getAccountId() {
        return this.shareAccount.getId() ;
    }
    
    public Long getChargeId() {
        return this.charge.getId() ;
    }
}
