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
package com.stellar.bnkbiz.portfolio.shares.data;

import java.math.BigDecimal;
import java.util.Date;

public class ShareMarketPriceData {

    private final Date startDate;
    
    private final BigDecimal shareValue;

    public ShareMarketPriceData(final Date startDate, final BigDecimal shareValue) {
        this.startDate = startDate ;
        this.shareValue = shareValue ;
    }
    
    public Date getStartDate() {
        return this.startDate;
    }

    public BigDecimal getShareValue() {
        return this.shareValue;
    }
}
