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
package com.stellar.bnkbiz.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;

import com.stellar.bnkbiz.portfolio.common.domain.PeriodFrequencyType;
import org.springframework.stereotype.Component;

@Component
public class AprCalculator {

    public BigDecimal calculateFrom(final PeriodFrequencyType interestPeriodFrequencyType, final BigDecimal interestRatePerPeriod) {
        BigDecimal defaultAnnualNominalInterestRate = BigDecimal.ZERO;
        switch (interestPeriodFrequencyType) {
            case DAYS:
                defaultAnnualNominalInterestRate = interestRatePerPeriod.multiply(BigDecimal.valueOf(365));
            break;
            case WEEKS:
                defaultAnnualNominalInterestRate = interestRatePerPeriod.multiply(BigDecimal.valueOf(52));
            break;
            case MONTHS:
                defaultAnnualNominalInterestRate = interestRatePerPeriod.multiply(BigDecimal.valueOf(12));
            break;
            case YEARS:
                defaultAnnualNominalInterestRate = interestRatePerPeriod.multiply(BigDecimal.valueOf(1));
            break;
            case INVALID:
            break;
        }

        return defaultAnnualNominalInterestRate;
    }
}