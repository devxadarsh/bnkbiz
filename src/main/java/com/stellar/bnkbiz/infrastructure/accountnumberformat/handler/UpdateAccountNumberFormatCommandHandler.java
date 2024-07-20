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
package com.stellar.bnkbiz.infrastructure.accountnumberformat.handler;

import javax.transaction.Transactional;

import com.stellar.bnkbiz.commands.annotation.CommandType;
import com.stellar.bnkbiz.commands.handler.NewCommandSourceHandler;
import com.stellar.bnkbiz.infrastructure.accountnumberformat.service.AccountNumberFormatWritePlatformService;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@CommandType(entity = "ACCOUNTNUMBERFORMAT", action = "UPDATE")
public class UpdateAccountNumberFormatCommandHandler implements NewCommandSourceHandler {

    private final AccountNumberFormatWritePlatformService accountNumberFormatWritePlatformService;

    @Autowired
    public UpdateAccountNumberFormatCommandHandler(final AccountNumberFormatWritePlatformService accountNumberFormatWritePlatformService) {
        this.accountNumberFormatWritePlatformService = accountNumberFormatWritePlatformService;
    }

    @Override
    @Transactional
    public CommandProcessingResult processCommand(JsonCommand command) {
        return this.accountNumberFormatWritePlatformService.updateAccountNumberFormat(command.entityId(), command);
    }

}
