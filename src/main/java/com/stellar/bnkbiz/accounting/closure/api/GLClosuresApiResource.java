package com.stellar.bnkbiz.accounting.closure.api;///**
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
//package com.stellar.bnkbiz.accounting.closure.api;
//
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import javax.ws.rs.Consumes;
//import javax.ws.rs.DELETE;
//import javax.ws.rs.GET;
//import javax.ws.rs.POST;
//import javax.ws.rs.PUT;
//import javax.ws.rs.Path;
//import javax.ws.rs.PathParam;
//import javax.ws.rs.Produces;
//import javax.ws.rs.QueryParam;
//import javax.ws.rs.core.Context;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.UriInfo;
//
//import com.stellar.bnkbiz.accounting.closure.data.GLClosureData;
//import com.stellar.bnkbiz.accounting.closure.service.GLClosureReadPlatformService;
//import com.stellar.bnkbiz.commands.domain.CommandWrapper;
//import com.stellar.bnkbiz.commands.service.CommandWrapperBuilder;
//import com.stellar.bnkbiz.commands.service.PortfolioCommandSourceWritePlatformService;
//import com.stellar.bnkbiz.infrastructure.core.api.ApiRequestParameterHelper;
//import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
//import com.stellar.bnkbiz.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
//import com.stellar.bnkbiz.infrastructure.core.serialization.DefaultToApiJsonSerializer;
//import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
//import com.stellar.bnkbiz.organisation.office.service.OfficeReadPlatformService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//
//@Path("/glclosures")
//@Component
//@Scope("singleton")
//public class GLClosuresApiResource {
//
//    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "officeId", "officeName",
//            "closingDate", "deleted", "createdDate", "lastUpdatedDate", "createdByUserId", "createdByUsername", "lastUpdatedByUserId",
//            "lastUpdatedByUsername"));
//
//    private final String resourceNameForPermission = "GLCLOSURE";
//
//    private final GLClosureReadPlatformService glClosureReadPlatformService;
//    private final OfficeReadPlatformService officeReadPlatformService;
//    private final DefaultToApiJsonSerializer<GLClosureData> apiJsonSerializerService;
//    private final ApiRequestParameterHelper apiRequestParameterHelper;
//    private final PlatformSecurityContext context;
//    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
//
//    @Autowired
//    public GLClosuresApiResource(final PlatformSecurityContext context, final GLClosureReadPlatformService glClosureReadPlatformService,
//            final DefaultToApiJsonSerializer<GLClosureData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
//            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
//            final OfficeReadPlatformService officeReadPlatformService) {
//        this.context = context;
//        this.apiRequestParameterHelper = apiRequestParameterHelper;
//        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
//        this.apiJsonSerializerService = toApiJsonSerializer;
//        this.glClosureReadPlatformService = glClosureReadPlatformService;
//        this.officeReadPlatformService = officeReadPlatformService;
//    }
//
//    @GET
//    @Consumes({ MediaType.APPLICATION_JSON })
//    @Produces({ MediaType.APPLICATION_JSON })
//    public String retrieveAllClosures(@Context final UriInfo uriInfo, @QueryParam("officeId") final Long officeId) {
//
//        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);
//        final List<GLClosureData> glClosureDatas = this.glClosureReadPlatformService.retrieveAllGLClosures(officeId);
//
//        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
//        return this.apiJsonSerializerService.serialize(settings, glClosureDatas, RESPONSE_DATA_PARAMETERS);
//    }
//
//    @GET
//    @Path("{glClosureId}")
//    @Consumes({ MediaType.APPLICATION_JSON })
//    @Produces({ MediaType.APPLICATION_JSON })
//    public String retreiveClosure(@PathParam("glClosureId") final Long glClosureId, @Context final UriInfo uriInfo) {
//
//        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);
//
//        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
//
//        final GLClosureData glClosureData = this.glClosureReadPlatformService.retrieveGLClosureById(glClosureId);
//        if (settings.isTemplate()) {
//            glClosureData.setAllowedOffices(this.officeReadPlatformService.retrieveAllOfficesForDropdown());
//        }
//
//        return this.apiJsonSerializerService.serialize(settings, glClosureData, RESPONSE_DATA_PARAMETERS);
//    }
//
//    @POST
//    @Consumes({ MediaType.APPLICATION_JSON })
//    @Produces({ MediaType.APPLICATION_JSON })
//    public String createGLClosure(final String jsonRequestBody) {
//
//        final CommandWrapper commandRequest = new CommandWrapperBuilder().createGLClosure().withJson(jsonRequestBody).build();
//
//        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
//
//        return this.apiJsonSerializerService.serialize(result);
//    }
//
//    @PUT
//    @Path("{glClosureId}")
//    @Consumes({ MediaType.APPLICATION_JSON })
//    @Produces({ MediaType.APPLICATION_JSON })
//    public String updateGLClosure(@PathParam("glClosureId") final Long glClosureId, final String jsonRequestBody) {
//        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateGLClosure(glClosureId).withJson(jsonRequestBody).build();
//
//        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
//
//        return this.apiJsonSerializerService.serialize(result);
//    }
//
//    @DELETE
//    @Path("{glClosureId}")
//    @Consumes({ MediaType.APPLICATION_JSON })
//    @Produces({ MediaType.APPLICATION_JSON })
//    public String deleteGLClosure(@PathParam("glClosureId") final Long glClosureId) {
//
//        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteGLClosure(glClosureId).build();
//
//        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
//
//        return this.apiJsonSerializerService.serialize(result);
//    }
//}



import com.stellar.bnkbiz.accounting.closure.data.GLClosureData;
import com.stellar.bnkbiz.accounting.closure.service.GLClosureReadPlatformService;
import com.stellar.bnkbiz.commands.domain.CommandWrapper;
import com.stellar.bnkbiz.commands.service.CommandWrapperBuilder;
import com.stellar.bnkbiz.commands.service.PortfolioCommandSourceWritePlatformService;
import com.stellar.bnkbiz.infrastructure.core.api.ApiRequestParameterHelper;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import com.stellar.bnkbiz.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
import com.stellar.bnkbiz.organisation.office.service.OfficeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

//import javax.ws.rs.QueryParam;
//import javax.ws.rs.core.Context;
//import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/glclosures")
public class GLClosuresApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "officeId", "officeName",
            "closingDate", "deleted", "createdDate", "lastUpdatedDate", "createdByUserId", "createdByUsername", "lastUpdatedByUserId",
            "lastUpdatedByUsername"));

    private final String resourceNameForPermission = "GLCLOSURE";

    private final GLClosureReadPlatformService glClosureReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final DefaultToApiJsonSerializer<GLClosureData> apiJsonSerializerService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public GLClosuresApiResource(final PlatformSecurityContext context, final GLClosureReadPlatformService glClosureReadPlatformService,
                                 final DefaultToApiJsonSerializer<GLClosureData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
                                 final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
                                 final OfficeReadPlatformService officeReadPlatformService) {
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiJsonSerializerService = toApiJsonSerializer;
        this.glClosureReadPlatformService = glClosureReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
    }

//    @GetMapping
//    public String retrieveAllClosures(@Context final UriInfo uriInfo, @Param("officeId") final Long officeId) {
//
//        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);
//        final List<GLClosureData> glClosureDatas = this.glClosureReadPlatformService.retrieveAllGLClosures(officeId);
//
//        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
//        return this.apiJsonSerializerService.serialize(settings, glClosureDatas, RESPONSE_DATA_PARAMETERS);
//    }

    @GetMapping
    public String retrieveAllClosures(@RequestParam(name = "officeId", required = false) Long officeId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        final List<GLClosureData> glClosureDatas = this.glClosureReadPlatformService.retrieveAllGLClosures(officeId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(request.getParameterMap());

        r

    @GetMapping("/{glClosureId}")
    public String retreiveClosure(@PathVariable("glClosureId") final Long glClosureId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final GLClosureData glClosureData = this.glClosureReadPlatformService.retrieveGLClosureById(glClosureId);
        if (settings.isTemplate()) {
            glClosureData.setAllowedOffices(this.officeReadPlatformService.retrieveAllOfficesForDropdown());
        }

        return this.apiJsonSerializerService.serialize(settings, glClosureData, RESPONSE_DATA_PARAMETERS);
    }

    @PostMapping
    public String createGLClosure(@RequestBody final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createGLClosure().withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @PutMapping("/{glClosureId}")
    public String updateGLClosure(@PathVariable("glClosureId") final Long glClosureId, @RequestBody final String jsonRequestBody) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateGLClosure(glClosureId).withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @DeleteMapping("/{glClosureId}")
    public String deleteGLClosure(@PathVariable("glClosureId") final Long glClosureId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteGLClosure(glClosureId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }
}
