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
package com.stellar.bnkbiz.organisation.teller.data;

import java.io.Serializable;
import java.math.BigDecimal;

public final class CashierTransactionTypeTotalsData implements Serializable {
	private final Integer cashierTxnType;
	private final BigDecimal cashTotal;

    private CashierTransactionTypeTotalsData (
    		final Integer cashierTxnType, 
    		final BigDecimal cashTotal
    		) {
    	this.cashierTxnType = cashierTxnType;
    	this.cashTotal = cashTotal;
    }

    public static CashierTransactionTypeTotalsData instance(
    		final Integer cashierTxnType, 
    		final BigDecimal cashTotal
    		) {
           return new CashierTransactionTypeTotalsData(
        	cashierTxnType, cashTotal);
    }

    public Integer getCashierTxnType() {
		return cashierTxnType;
	}
    
    public BigDecimal getCashTotal() {
		return cashTotal;
	}

}
