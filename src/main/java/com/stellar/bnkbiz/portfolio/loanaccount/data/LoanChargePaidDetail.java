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
package com.stellar.bnkbiz.portfolio.loanaccount.data;

import com.stellar.bnkbiz.organisation.monetary.domain.Money;
import com.stellar.bnkbiz.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;


public class LoanChargePaidDetail {

    private final Money amount;
    private final LoanRepaymentScheduleInstallment installment;
    private final boolean isFeeCharge;
    
    public LoanChargePaidDetail( Money amount,LoanRepaymentScheduleInstallment installment,boolean isFeeCharge){
        this.amount = amount;
        this.installment = installment;
        this.isFeeCharge = isFeeCharge;
    }

    
    public Money getAmount() {
        return this.amount;
    }

    
    public LoanRepaymentScheduleInstallment getInstallment() {
        return this.installment;
    }

    
    public boolean isFeeCharge() {
        return this.isFeeCharge;
    }
}
