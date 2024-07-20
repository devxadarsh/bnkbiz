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
package com.stellar.bnkbiz.accounting.provisioning.handler;

import com.stellar.bnkbiz.accounting.provisioning.service.ProvisioningEntriesWritePlatformService;
import com.stellar.bnkbiz.commands.annotation.CommandType;
import com.stellar.bnkbiz.commands.handler.NewCommandSourceHandler;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CommandType(entity = "PROVISIONENTRIES", action = "CREATE")
public class CreateProvisioningEntriesRequestCommandHandler implements NewCommandSourceHandler {

    private final ProvisioningEntriesWritePlatformService provisioningEntriesWritePlatformService ;
    @Autowired
    public CreateProvisioningEntriesRequestCommandHandler(
            final ProvisioningEntriesWritePlatformService provisioningEntriesWritePlatformService) {
        this.provisioningEntriesWritePlatformService = provisioningEntriesWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(JsonCommand jsonCommand) {
        return this.provisioningEntriesWritePlatformService.createProvisioningEntries(jsonCommand) ;
    }

}
