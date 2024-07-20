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
package com.stellar.bnkbiz.portfolio.loanaccount.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
import com.stellar.bnkbiz.organisation.office.data.OfficeData;
import com.stellar.bnkbiz.organisation.office.service.OfficeReadPlatformService;
import com.stellar.bnkbiz.organisation.staff.data.BulkTransferLoanOfficerData;
import com.stellar.bnkbiz.organisation.staff.data.StaffAccountSummaryCollectionData;
import com.stellar.bnkbiz.organisation.staff.data.StaffData;
import com.stellar.bnkbiz.organisation.staff.service.StaffReadPlatformService;
import com.stellar.bnkbiz.portfolio.loanaccount.service.BulkLoansReadPlatformService;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loans/loanreassignment")
@Component
@Scope("singleton")
public class BulkLoansApiResource {

    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("officeId", "fromLoanOfficerId",
            "assignmentDate", "officeOptions", "loanOfficerOptions", "accountSummaryCollection"));

    private final String resourceNameForPermissions = "LOAN";

    private final PlatformSecurityContext context;
    private final StaffReadPlatformService staffReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final BulkLoansReadPlatformService bulkLoansReadPlatformService;
    private final DefaultToApiJsonSerializer<BulkTransferLoanOfficerData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public BulkLoansApiResource(final PlatformSecurityContext context, final StaffReadPlatformService staffReadPlatformService,
            final OfficeReadPlatformService officeReadPlatformService, final BulkLoansReadPlatformService bulkLoansReadPlatformService,
            final DefaultToApiJsonSerializer<BulkTransferLoanOfficerData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.staffReadPlatformService = staffReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.bulkLoansReadPlatformService = bulkLoansReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String loanReassignmentTemplate(@QueryParam("officeId") final Long officeId,
            @QueryParam("fromLoanOfficerId") final Long loanOfficerId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<OfficeData> offices = this.officeReadPlatformService.retrieveAllOfficesForDropdown();

        Collection<StaffData> loanOfficers = null;
        StaffAccountSummaryCollectionData staffAccountSummaryCollectionData = null;

        if (officeId != null) {
            loanOfficers = this.staffReadPlatformService.retrieveAllLoanOfficersInOfficeById(officeId);
        }

        if (loanOfficerId != null) {
            staffAccountSummaryCollectionData = this.bulkLoansReadPlatformService.retrieveLoanOfficerAccountSummary(loanOfficerId);
        }

        final BulkTransferLoanOfficerData loanReassignmentData = BulkTransferLoanOfficerData.templateForBulk(officeId, loanOfficerId,
                new LocalDate(), offices, loanOfficers, staffAccountSummaryCollectionData);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanReassignmentData, this.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String loanReassignment(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().assignLoanOfficersInBulk().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}