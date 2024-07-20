///**
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements. See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership. The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License. You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied. See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package com.stellar.bnkbiz.accounting.accrual.api;
//
//import javax.ws.rs.Consumes;
//import javax.ws.rs.POST;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.MediaType;
//
//import com.stellar.bnkbiz.commands.domain.CommandWrapper;
//import com.stellar.bnkbiz.commands.service.CommandWrapperBuilder;
//import com.stellar.bnkbiz.commands.service.PortfolioCommandSourceWritePlatformService;
//import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
//import com.stellar.bnkbiz.infrastructure.core.serialization.DefaultToApiJsonSerializer;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//
//@Path("/runaccruals")
//@Component
//@Scope("singleton")
//public class AccrualAccountingApiResource {
//
//    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
//    private final DefaultToApiJsonSerializer<String> apiJsonSerializerService;
//
//    @Autowired
//    public AccrualAccountingApiResource(final DefaultToApiJsonSerializer<String> apiJsonSerializerService,
//            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
//        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
//        this.apiJsonSerializerService = apiJsonSerializerService;
//
//    }
//
//    @POST
//    @Consumes({ MediaType.APPLICATION_JSON })
//    @Produces({ MediaType.APPLICATION_JSON })
//    public String executePeriodicAccrualAccounting(final String jsonRequestBody) {
//
//        final CommandWrapper commandRequest = new CommandWrapperBuilder().excuteAccrualAccounting().withJson(jsonRequestBody).build();
//
//        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
//
//        return this.apiJsonSerializerService.serialize(result);
//    }
//
//}

package com.stellar.bnkbiz.accounting.accrual.api;

import com.stellar.bnkbiz.commands.domain.CommandWrapper;
import com.stellar.bnkbiz.commands.service.CommandWrapperBuilder;
import com.stellar.bnkbiz.commands.service.PortfolioCommandSourceWritePlatformService;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/runaccruals")
public class AccrualAccountingApiResource {

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<String> apiJsonSerializerService;

    public AccrualAccountingApiResource(DefaultToApiJsonSerializer<String> apiJsonSerializerService,
                                        PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String executePeriodicAccrualAccounting(@RequestBody String jsonRequestBody) {
        CommandWrapper commandRequest = new CommandWrapperBuilder().excuteAccrualAccounting().withJson(jsonRequestBody).build();
        CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return apiJsonSerializerService.serialize(result);
    }
}
