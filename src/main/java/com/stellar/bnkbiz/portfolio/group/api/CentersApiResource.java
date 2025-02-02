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
package com.stellar.bnkbiz.portfolio.group.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import com.stellar.bnkbiz.accounting.journalentry.api.DateParam;
import com.stellar.bnkbiz.commands.domain.CommandWrapper;
import com.stellar.bnkbiz.commands.service.CommandWrapperBuilder;
import com.stellar.bnkbiz.commands.service.PortfolioCommandSourceWritePlatformService;
import com.stellar.bnkbiz.infrastructure.core.api.ApiParameterHelper;
import com.stellar.bnkbiz.infrastructure.core.api.ApiRequestParameterHelper;
import com.stellar.bnkbiz.infrastructure.core.api.JsonQuery;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.data.PaginationParameters;
import com.stellar.bnkbiz.infrastructure.core.exception.UnrecognizedQueryParamException;
import com.stellar.bnkbiz.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import com.stellar.bnkbiz.infrastructure.core.serialization.FromJsonHelper;
import com.stellar.bnkbiz.infrastructure.core.serialization.ToApiJsonSerializer;
import com.stellar.bnkbiz.infrastructure.core.service.Page;
import com.stellar.bnkbiz.infrastructure.core.service.SearchParameters;
import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
import com.stellar.bnkbiz.portfolio.accountdetails.data.AccountSummaryCollectionData;
import com.stellar.bnkbiz.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import com.stellar.bnkbiz.portfolio.calendar.data.CalendarData;
import com.stellar.bnkbiz.portfolio.calendar.domain.CalendarEntityType;
import com.stellar.bnkbiz.portfolio.calendar.service.CalendarReadPlatformService;
import com.stellar.bnkbiz.portfolio.collectionsheet.data.JLGCollectionSheetData;
import com.stellar.bnkbiz.portfolio.collectionsheet.service.CollectionSheetReadPlatformService;
import com.stellar.bnkbiz.portfolio.group.data.CenterData;
import com.stellar.bnkbiz.portfolio.group.data.GroupGeneralData;
import com.stellar.bnkbiz.portfolio.group.data.StaffCenterData;
import com.stellar.bnkbiz.portfolio.group.service.CenterReadPlatformService;
import com.stellar.bnkbiz.portfolio.meeting.data.MeetingData;
import com.stellar.bnkbiz.portfolio.meeting.service.MeetingReadPlatformService;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Path("/centers")
@Component
@Scope("singleton")
public class CentersApiResource {

    private final PlatformSecurityContext context;
    private final CenterReadPlatformService centerReadPlatformService;
    private final ToApiJsonSerializer<CenterData> centerApiJsonSerializer;
    private final ToApiJsonSerializer<Object> toApiJsonSerializer;
    private final ToApiJsonSerializer<AccountSummaryCollectionData> groupSummaryToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final CollectionSheetReadPlatformService collectionSheetReadPlatformService;
    private final FromJsonHelper fromJsonHelper;
    private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final MeetingReadPlatformService meetingReadPlatformService;

    @Autowired
    public CentersApiResource(final PlatformSecurityContext context, final CenterReadPlatformService centerReadPlatformService,
            final ToApiJsonSerializer<CenterData> centerApiJsonSerializer, final ToApiJsonSerializer<Object> toApiJsonSerializer,
            final ToApiJsonSerializer<AccountSummaryCollectionData> groupSummaryToApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final CollectionSheetReadPlatformService collectionSheetReadPlatformService, final FromJsonHelper fromJsonHelper,
            final AccountDetailsReadPlatformService accountDetailsReadPlatformService,
            final CalendarReadPlatformService calendarReadPlatformService, final MeetingReadPlatformService meetingReadPlatformService) {
        this.context = context;
        this.centerReadPlatformService = centerReadPlatformService;
        this.centerApiJsonSerializer = centerApiJsonSerializer;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.groupSummaryToApiJsonSerializer = groupSummaryToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.collectionSheetReadPlatformService = collectionSheetReadPlatformService;
        this.fromJsonHelper = fromJsonHelper;
        this.accountDetailsReadPlatformService = accountDetailsReadPlatformService;
        this.calendarReadPlatformService = calendarReadPlatformService;
        this.meetingReadPlatformService = meetingReadPlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@Context final UriInfo uriInfo, @QueryParam("command") final String commandParam,
            @QueryParam("officeId") final Long officeId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly) {

        this.context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.CENTER_RESOURCE_NAME);

        if (is(commandParam, "close")) {
            final CenterData centerClosureTemplate = this.centerReadPlatformService.retrieveCenterWithClosureReasons();
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.centerApiJsonSerializer.serialize(settings, centerClosureTemplate,
                    GroupingTypesApiConstants.CENTER_RESPONSE_DATA_PARAMETERS);
        }

        final CenterData template = this.centerReadPlatformService.retrieveTemplate(officeId, staffInSelectedOfficeOnly);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.centerApiJsonSerializer.serialize(settings, template, GroupingTypesApiConstants.CENTER_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") final String sqlSearch,
            @QueryParam("officeId") final Long officeId, @QueryParam("staffId") final Long staffId,
            @QueryParam("externalId") final String externalId, @QueryParam("name") final String name,
            @QueryParam("underHierarchy") final String hierarchy, @QueryParam("paged") final Boolean paged,
            @QueryParam("offset") final Integer offset, @QueryParam("limit") final Integer limit,
            @QueryParam("orderBy") final String orderBy, @QueryParam("sortOrder") final String sortOrder,
            @QueryParam("meetingDate") final DateParam meetingDateParam, @QueryParam("dateFormat") final String dateFormat,
            @QueryParam("locale") final String locale) {

        this.context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.CENTER_RESOURCE_NAME);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (meetingDateParam != null && officeId != null) {
            Date meetingDate = meetingDateParam.getDate("meetingDate", dateFormat, locale);
            Collection<StaffCenterData> staffCenterDataArray = this.centerReadPlatformService.retriveAllCentersByMeetingDate(officeId,
                    meetingDate, staffId);
            return this.toApiJsonSerializer.serialize(settings, staffCenterDataArray,
                    GroupingTypesApiConstants.STAFF_CENTER_RESPONSE_DATA_PARAMETERS);
        }
        final PaginationParameters parameters = PaginationParameters.instance(paged, offset, limit, orderBy, sortOrder);
        final Boolean isOrphansOnly = false;
        final SearchParameters searchParameters = SearchParameters.forGroups(sqlSearch, officeId, staffId, externalId, name, hierarchy,
                offset, limit, orderBy, sortOrder, isOrphansOnly);
        if (parameters.isPaged()) {
            final Page<CenterData> centers = this.centerReadPlatformService.retrievePagedAll(searchParameters, parameters);
            return this.toApiJsonSerializer.serialize(settings, centers, GroupingTypesApiConstants.CENTER_RESPONSE_DATA_PARAMETERS);
        }

        final Collection<CenterData> centers = this.centerReadPlatformService.retrieveAll(searchParameters, parameters);
        return this.toApiJsonSerializer.serialize(settings, centers, GroupingTypesApiConstants.CENTER_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{centerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@Context final UriInfo uriInfo, @PathParam("centerId") final Long centerId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly) {

        this.context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.CENTER_RESOURCE_NAME);
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        CalendarData collectionMeetingCalendar = null;
        Collection<GroupGeneralData> groups = null;
        CenterData center = this.centerReadPlatformService.retrieveOne(centerId);

        final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
        if (template) {
            final CenterData templateCenter = this.centerReadPlatformService.retrieveTemplate(center.officeId(), staffInSelectedOfficeOnly);
            center = CenterData.withTemplate(templateCenter, center);
        }

        if (!associationParameters.isEmpty()) {
            if (associationParameters.contains("groupMembers")) {
                groups = this.centerReadPlatformService.retrieveAssociatedGroups(centerId);
            }

            if (associationParameters.contains("collectionMeetingCalendar")) {
                collectionMeetingCalendar = this.calendarReadPlatformService.retrieveCollctionCalendarByEntity(centerId,
                        CalendarEntityType.CENTERS.getValue());
                if (collectionMeetingCalendar != null) {
                    final boolean withHistory = true;
                    final LocalDate tillDate = null;
                    final Collection<LocalDate> recurringDates = this.calendarReadPlatformService.generateRecurringDates(
                            collectionMeetingCalendar, withHistory, tillDate);
                    final Collection<LocalDate> nextTenRecurringDates = this.calendarReadPlatformService
                            .generateNextTenRecurringDates(collectionMeetingCalendar);
                    final MeetingData lastMeeting = this.meetingReadPlatformService.retrieveLastMeeting(collectionMeetingCalendar
                            .getCalendarInstanceId());
                    final LocalDate recentEligibleMeetingDate = this.calendarReadPlatformService
                            .generateNextEligibleMeetingDateForCollection(collectionMeetingCalendar, lastMeeting);
                    collectionMeetingCalendar = CalendarData.withRecurringDates(collectionMeetingCalendar, recurringDates,
                            nextTenRecurringDates, recentEligibleMeetingDate);
                }
            }

            center = CenterData.withAssociations(center, groups, collectionMeetingCalendar);
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.centerApiJsonSerializer.serialize(settings, center, GroupingTypesApiConstants.CENTER_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String create(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createCenter() //
                .withJson(apiRequestBodyAsJson) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);

    }

    @PUT
    @Path("{centerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String update(@PathParam("centerId") final Long centerId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateCenter(centerId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{centerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String delete(@PathParam("centerId") final Long centerId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteCenter(centerId) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{centerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String activate(@PathParam("centerId") final Long centerId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson, @Context final UriInfo uriInfo) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;
        if (is(commandParam, "activate")) {
            final CommandWrapper commandRequest = builder.activateCenter(centerId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "generateCollectionSheet")) {
            final JsonElement parsedQuery = this.fromJsonHelper.parse(apiRequestBodyAsJson);
            final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, this.fromJsonHelper);
            final JLGCollectionSheetData collectionSheet = this.collectionSheetReadPlatformService.generateCenterCollectionSheet(centerId,
                    query);
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, collectionSheet, GroupingTypesApiConstants.COLLECTIONSHEET_DATA_PARAMETERS);
        } else if (is(commandParam, "saveCollectionSheet")) {
            final CommandWrapper commandRequest = builder.saveCenterCollectionSheet(centerId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "close")) {
            final CommandWrapper commandRequest = builder.closeCenter(centerId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "associateGroups")) {
            final CommandWrapper commandRequest = builder.associateGroupsToCenter(centerId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "disassociateGroups")) {
            final CommandWrapper commandRequest = builder.disassociateGroupsFromCenter(centerId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "activate", "generateCollectionSheet",
                    "saveCollectionSheet", "close", "associateGroups", "disassociateGroups" });
        }

    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("{centerId}/accounts")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveGroupAccount(@PathParam("centerId") final Long centerId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.CENTER_RESOURCE_NAME);

        final AccountSummaryCollectionData groupAccount = this.accountDetailsReadPlatformService.retrieveGroupAccountDetails(centerId);

        final Set<String> GROUP_ACCOUNTS_DATA_PARAMETERS = new HashSet<>(Arrays.asList("loanAccounts", "savingsAccounts",
                "memberLoanAccounts", "memberSavingsAccounts"));

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.groupSummaryToApiJsonSerializer.serialize(settings, groupAccount, GROUP_ACCOUNTS_DATA_PARAMETERS);
    }
}