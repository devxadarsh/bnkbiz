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
package com.stellar.bnkbiz.infrastructure.configuration.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import com.stellar.bnkbiz.infrastructure.configuration.data.ExternalServicesPropertiesData;
import com.stellar.bnkbiz.infrastructure.configuration.service.ExternalServicesPropertiesReadPlatformService;
import com.stellar.bnkbiz.infrastructure.core.api.ApiRequestParameterHelper;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import com.stellar.bnkbiz.infrastructure.core.serialization.ToApiJsonSerializer;
import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/externalservice")
@Component
@Scope("singleton")
public class ExternalServicesConfigurationApiResource {

    private final PlatformSecurityContext context;
    private final ExternalServicesPropertiesReadPlatformService externalServicePropertiesReadPlatformService;
    private final ToApiJsonSerializer<ExternalServicesPropertiesData> toApiJsonSerializer;
    // private final ToApiJsonSerializer<S3CredentialsData>
    // s3ToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ExternalServicesConfigurationApiResource(final PlatformSecurityContext context,
            final ExternalServicesPropertiesReadPlatformService readPlatformService,
            final ToApiJsonSerializer<ExternalServicesPropertiesData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.externalServicePropertiesReadPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        // this.s3ToApiJsonSerializer = s3ToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Path("{servicename}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@PathParam("servicename") final String serviceName, @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(ExternalServiceConfigurationApiConstant.EXTERNAL_SERVICE_RESOURCE_NAME);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        final Collection<ExternalServicesPropertiesData> externalServiceNVPs = this.externalServicePropertiesReadPlatformService
                .retrieveOne(serviceName);
        return this.toApiJsonSerializer.serialize(settings, externalServiceNVPs,
                ExternalServiceConfigurationApiConstant.EXTERNAL_SERVICE_CONFIGURATION_DATA_PARAMETERS);
    }

    @PUT
    @Path("{servicename}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateExternalServiceProperties(@PathParam("servicename") final String serviceName, final String apiRequestBodyAsJson) {
        // ExternalServicesData external =
        // this.externalServiceReadPlatformService.getExternalServiceDetailsByServiceName(serviceName);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateExternalServiceProperties(serviceName)
                .withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);

    }
}
