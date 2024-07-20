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
package com.stellar.bnkbiz.organisation.provisioning.domain;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.stellar.bnkbiz.accounting.glaccount.domain.GLAccount;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.domain.AbstractAuditableCustom;
import com.stellar.bnkbiz.organisation.provisioning.constants.ProvisioningCriteriaConstants;
import com.stellar.bnkbiz.organisation.provisioning.data.ProvisioningCriteriaDefinitionData;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.LoanProduct;
import com.stellar.bnkbiz.useradministration.domain.AppUser;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.DateTime;

@Entity
@Table(name = "m_provisioning_criteria", uniqueConstraints = { @UniqueConstraint(columnNames = { "criteria_name" }, name = "criteria_name") })
public class ProvisioningCriteria extends AbstractAuditableCustom<AppUser, Long> {

    @Column(name = "criteria_name", nullable = false)
    private String criteriaName;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "criteria", orphanRemoval = true)
    Set<ProvisioningCriteriaDefinition> provisioningCriteriaDefinition = new HashSet<>();

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "criteria", orphanRemoval = true)
    Set<LoanProductProvisionCriteria> loanProductMapping = new HashSet<>();

    public String getCriteriaName() {
        return this.criteriaName;
    }

    public void setCriteriaName(String criteriaName) {
        this.criteriaName = criteriaName;
    }

    protected ProvisioningCriteria() {
        
    }
    
    public ProvisioningCriteria(String criteriaName, AppUser createdBy, DateTime createdDate, AppUser lastModifiedBy, DateTime lastModifiedDate) {
        this.criteriaName = criteriaName;
        setCreatedBy(createdBy) ;
        setCreatedDate(createdDate) ;
        setLastModifiedBy(lastModifiedBy) ;
        setLastModifiedDate(lastModifiedDate) ;
    }

    public void setProvisioningCriteriaDefinitions(Set<ProvisioningCriteriaDefinition> provisioningCriteriaDefinition) {
        this.provisioningCriteriaDefinition.clear();
        this.provisioningCriteriaDefinition.addAll(provisioningCriteriaDefinition);
    }

    public void setLoanProductProvisioningCriteria(Set<LoanProductProvisionCriteria> loanProductMapping) {
        this.loanProductMapping.clear();
        this.loanProductMapping.addAll(loanProductMapping);
    }
    
    public Map<String, Object> update(JsonCommand command, List<LoanProduct> loanProducts) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);
        if(command.isChangeInStringParameterNamed(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM, criteriaName)) {
            final String valueAsInput = command.stringValueOfParameterNamed(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM);
            actualChanges.put(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM, valueAsInput);
            this.criteriaName = valueAsInput ;
        }

        Set<LoanProductProvisionCriteria> temp = new HashSet<>() ;
        Set<LoanProduct> productsTemp = new HashSet<>() ;
        
        for(LoanProductProvisionCriteria mapping: loanProductMapping) {
            if(!loanProducts.contains(mapping.getLoanProduct())) {
                temp.add(mapping) ;
            }else {
                productsTemp.add(mapping.getLoanProduct()) ;
            }
        }
        loanProductMapping.removeAll(temp) ;
        
        for(LoanProduct loanProduct: loanProducts) {
            if(!productsTemp.contains(loanProduct)) {
                this.loanProductMapping.add( new LoanProductProvisionCriteria(this, loanProduct)) ;     
            }
        }
        
        actualChanges.put(ProvisioningCriteriaConstants.JSON_LOANPRODUCTS_PARAM, loanProductMapping);
        return actualChanges ;
    }
    
    public void update(ProvisioningCriteriaDefinitionData data, GLAccount liability, GLAccount expense) {
        for(ProvisioningCriteriaDefinition def: provisioningCriteriaDefinition) {
            if(data.getId() == def.getId()) {
                def.update(data.getMinAge(), data.getMaxAge(), data.getProvisioningPercentage(), liability, expense) ;
                break ;
            }
        }
    }
}
