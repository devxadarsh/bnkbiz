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
package com.stellar.bnkbiz.portfolio.account.service;

import java.util.Collection;

import com.stellar.bnkbiz.infrastructure.core.service.Page;
import com.stellar.bnkbiz.portfolio.account.data.StandingInstructionDTO;
import com.stellar.bnkbiz.portfolio.account.data.StandingInstructionData;
import com.stellar.bnkbiz.portfolio.account.data.StandingInstructionDuesData;

public interface StandingInstructionReadPlatformService {

    StandingInstructionData retrieveTemplate(Long fromOfficeId, Long fromClientId, Long fromAccountId, Integer fromAccountType,
            Long toOfficeId, Long toClientId, Long toAccountId, Integer toAccountType, Integer transferType);

    Page<StandingInstructionData> retrieveAll(StandingInstructionDTO standingInstructionDTO);

    StandingInstructionData retrieveOne(Long instructionId);

    Collection<StandingInstructionData> retrieveAll(Integer status);

    StandingInstructionDuesData retriveLoanDuesData(Long loanId);

}