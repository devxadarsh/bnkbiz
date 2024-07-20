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

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import com.stellar.bnkbiz.portfolio.accounts.data.PurchasedSharesData;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_purchasedshares")
public class PurchasedShares extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private ShareAccount shareAccount ;
    
    @Column(name = "purchased_date")
    @Temporal(TemporalType.DATE)
    private Date purchasedDate ;
    
    @Column(name = "share_count")
    private Long totalShares ;
    
    @Column(name = "share_price")
    private BigDecimal shareValue ; 
    
    @Column(name ="status")
    private String status = "Submitted";
    
    protected PurchasedShares() {
        
    }
    
    public void setShareAccount(final ShareAccount shareAccount) {
        this.shareAccount = shareAccount ;
    }
    
    public PurchasedShares(final Date purchasedDate, final Long totalShares, final BigDecimal shareValue) {
        this.purchasedDate = purchasedDate ;
        this.totalShares = totalShares ;
        this.shareValue = shareValue ;
    }
    
    public PurchasedSharesData toData() {
        return new PurchasedSharesData(this.purchasedDate, this.totalShares, this.shareValue, this.status) ;
    }
    
    public Date getPurchasedDate() {
        return this.purchasedDate ;
    }
    
    public Long getTotalShares() {
        return this.totalShares ;
    }
    
    public BigDecimal getPurchasePrice() {
        return this.shareValue ;
    }
    
    public String getStatus() {
        return this.status ;
    }
    
    public void setStatus(String status) {
        this.status = status ;
    }
}
