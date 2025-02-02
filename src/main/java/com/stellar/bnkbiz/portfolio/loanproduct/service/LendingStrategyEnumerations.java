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
package com.stellar.bnkbiz.portfolio.loanproduct.service;

import com.stellar.bnkbiz.infrastructure.core.data.EnumOptionData;
import com.stellar.bnkbiz.portfolio.loanproduct.domain.LendingStrategy;

public class LendingStrategyEnumerations {

    public static EnumOptionData lendingStrategy(final Integer id) {
        return lendingStrategy(LendingStrategy.fromInt(id));
    }

    public static EnumOptionData lendingStrategy(final LendingStrategy type) {
        EnumOptionData optionData = null;
        switch (type) {
            case INDIVIDUAL_LOAN:
                optionData = new EnumOptionData(type.getId().longValue(), type.getCode(), "Individual loan");
            break;
            case GROUP_LOAN:
                optionData = new EnumOptionData(type.getId().longValue(), type.getCode(), "Group loan");
            break;
            case JOINT_LIABILITY_LOAN:
                optionData = new EnumOptionData(type.getId().longValue(), type.getCode(), "Joint liability loan");
            break;
            case LINKED_LOAN:
                optionData = new EnumOptionData(type.getId().longValue(), type.getCode(), "Linked loan");
            break;

            default:
                optionData = new EnumOptionData(LendingStrategy.INVALID.getId().longValue(), LendingStrategy.INVALID.getCode(), "Invalid");
            break;

        }
        return optionData;
    }

}
