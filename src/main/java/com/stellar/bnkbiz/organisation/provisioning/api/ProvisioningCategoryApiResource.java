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
package com.stellar.bnkbiz.organisation.provisioning.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.stellar.bnkbiz.commands.domain.CommandWrapper;
import com.stellar.bnkbiz.commands.service.CommandWrapperBuilder;
import com.stellar.bnkbiz.commands.service.PortfolioCommandSourceWritePlatformService;
import com.stellar.bnkbiz.infrastructure.core.api.ApiRequestParameterHelper;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import com.stellar.bnkbiz.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
import com.stellar.bnkbiz.organisation.provisioning.data.ProvisioningCategoryData;
import com.stellar.bnkbiz.organisation.provisioning.service.ProvisioningCategoryReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/provisioningcategory")
@Component
@Scope("singleton")
public class ProvisioningCategoryApiResource {

    private final PlatformSecurityContext platformSecurityContext;
    private final ProvisioningCategoryReadPlatformService provisioningCategoryReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<ProvisioningCategoryData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ProvisioningCategoryApiResource(final PlatformSecurityContext platformSecurityContext,
            final ProvisioningCategoryReadPlatformService provisioningCategoryReadPlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final DefaultToApiJsonSerializer<ProvisioningCategoryData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.platformSecurityContext = platformSecurityContext;
        this.provisioningCategoryReadPlatformService = provisioningCategoryReadPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@Context final UriInfo uriInfo) {
        this.platformSecurityContext.authenticatedUser();
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        Collection<ProvisioningCategoryData> provisionCategoriesSet = null;
        provisionCategoriesSet = this.provisioningCategoryReadPlatformService.retrieveAllProvisionCategories();
        return this.toApiJsonSerializer.serialize(settings, provisionCategoriesSet);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createProvisioningCategory(final String apiRequestBodyAsJson) {
        CommandWrapper commandWrapper = null;
        this.platformSecurityContext.authenticatedUser();
        commandWrapper = new CommandWrapperBuilder().createProvisioningCategory().withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        return this.toApiJsonSerializer.serialize(commandProcessingResult);
    }

    @PUT
    @Path("{categoryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateProvisioningCategory(@PathParam("categoryId") final Long categoryId, final String apiRequestBodyAsJson) {
        this.platformSecurityContext.authenticatedUser();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateProvisioningCategory(categoryId)
                .withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{categoryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteProvisioningCategory(@PathParam("categoryId") final Long categoryId) {
        this.platformSecurityContext.authenticatedUser();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteProvisioningCategory(categoryId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
}
