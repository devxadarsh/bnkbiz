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
import java.util.HashSet;
import java.util.List;
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
import com.stellar.bnkbiz.portfolio.calendar.service.CalendarUtils;
import com.stellar.bnkbiz.portfolio.client.data.ClientData;
import com.stellar.bnkbiz.portfolio.client.service.ClientReadPlatformService;
import com.stellar.bnkbiz.portfolio.collectionsheet.data.JLGCollectionSheetData;
import com.stellar.bnkbiz.portfolio.collectionsheet.service.CollectionSheetReadPlatformService;
import com.stellar.bnkbiz.portfolio.group.data.GroupGeneralData;
import com.stellar.bnkbiz.portfolio.group.data.GroupRoleData;
import com.stellar.bnkbiz.portfolio.group.service.CenterReadPlatformService;
import com.stellar.bnkbiz.portfolio.group.service.GroupReadPlatformService;
import com.stellar.bnkbiz.portfolio.group.service.GroupRolesReadPlatformService;
import com.stellar.bnkbiz.portfolio.meeting.data.MeetingData;
import com.stellar.bnkbiz.portfolio.meeting.service.MeetingReadPlatformService;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.gson.JsonElement;

@Path("/groups")
@Component
@Scope("singleton")
public class GroupsApiResource {

    private final PlatformSecurityContext context;
    private final GroupReadPlatformService groupReadPlatformService;
    private final CenterReadPlatformService centerReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final ToApiJsonSerializer<Object> toApiJsonSerializer;
    private final ToApiJsonSerializer<GroupGeneralData> groupGeneralApiJsonSerializer;
    private final ToApiJsonSerializer<AccountSummaryCollectionData> groupSummaryToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final CollectionSheetReadPlatformService collectionSheetReadPlatformService;
    private final FromJsonHelper fromJsonHelper;
    private final GroupRolesReadPlatformService groupRolesReadPlatformService;
    private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final MeetingReadPlatformService meetingReadPlatformService;

    @Autowired
    public GroupsApiResource(final PlatformSecurityContext context, final GroupReadPlatformService groupReadPlatformService,
            final CenterReadPlatformService centerReadPlatformService, final ClientReadPlatformService clientReadPlatformService,
            final ToApiJsonSerializer<Object> toApiJsonSerializer,
            final ToApiJsonSerializer<GroupGeneralData> groupTopOfHierarchyApiJsonSerializer,
            final ToApiJsonSerializer<AccountSummaryCollectionData> groupSummaryToApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final CollectionSheetReadPlatformService collectionSheetReadPlatformService, final FromJsonHelper fromJsonHelper,
            final GroupRolesReadPlatformService groupRolesReadPlatformService,
            final AccountDetailsReadPlatformService accountDetailsReadPlatformService,
            final CalendarReadPlatformService calendarReadPlatformService, final MeetingReadPlatformService meetingReadPlatformService) {

        this.context = context;
        this.groupReadPlatformService = groupReadPlatformService;
        this.centerReadPlatformService = centerReadPlatformService;
        this.clientReadPlatformService = clientReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.groupGeneralApiJsonSerializer = groupTopOfHierarchyApiJsonSerializer;
        this.groupSummaryToApiJsonSerializer = groupSummaryToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.collectionSheetReadPlatformService = collectionSheetReadPlatformService;
        this.fromJsonHelper = fromJsonHelper;
        this.groupRolesReadPlatformService = groupRolesReadPlatformService;
        this.accountDetailsReadPlatformService = accountDetailsReadPlatformService;
        this.calendarReadPlatformService = calendarReadPlatformService;
        this.meetingReadPlatformService = meetingReadPlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@Context final UriInfo uriInfo, @QueryParam("officeId") final Long officeId,
            @QueryParam("center") final boolean isCenterGroup, @QueryParam("centerId") final Long centerId,
            @QueryParam("command") final String commandParam,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly) {

        this.context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);

        if (is(commandParam, "close")) {
            final GroupGeneralData groupClosureTemplate = this.groupReadPlatformService.retrieveGroupWithClosureReasons();
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.groupGeneralApiJsonSerializer.serialize(settings, groupClosureTemplate,
                    GroupingTypesApiConstants.GROUP_RESPONSE_DATA_PARAMETERS);
        }

        if (centerId != null) {
            final GroupGeneralData centerGroupTemplate = this.centerReadPlatformService.retrieveCenterGroupTemplate(centerId);
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.groupGeneralApiJsonSerializer.serialize(settings, centerGroupTemplate,
                    GroupingTypesApiConstants.CENTER_GROUP_RESPONSE_DATA_PARAMETERS);
        }

        final GroupGeneralData groupTemplate = this.groupReadPlatformService.retrieveTemplate(officeId, isCenterGroup,
                staffInSelectedOfficeOnly);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.groupGeneralApiJsonSerializer.serialize(settings, groupTemplate,
                GroupingTypesApiConstants.GROUP_RESPONSE_DATA_PARAMETERS);
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
            @QueryParam("orphansOnly") final Boolean orphansOnly) {

        this.context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);
        final PaginationParameters parameters = PaginationParameters.instance(paged, offset, limit, orderBy, sortOrder);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final SearchParameters searchParameters = SearchParameters.forGroups(sqlSearch, officeId, staffId, externalId, name, hierarchy,
                offset, limit, orderBy, sortOrder, orphansOnly);
        if (parameters.isPaged()) {
            final Page<GroupGeneralData> groups = this.groupReadPlatformService.retrievePagedAll(searchParameters, parameters);
            return this.toApiJsonSerializer.serialize(settings, groups, GroupingTypesApiConstants.GROUP_RESPONSE_DATA_PARAMETERS);
        }

        final Collection<GroupGeneralData> groups = this.groupReadPlatformService.retrieveAll(searchParameters, parameters);
        return this.toApiJsonSerializer.serialize(settings, groups, GroupingTypesApiConstants.GROUP_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@Context final UriInfo uriInfo, @PathParam("groupId") final Long groupId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @QueryParam("roleId") final Long roleId) {

        this.context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());

        GroupGeneralData group = this.groupReadPlatformService.retrieveOne(groupId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        // associations
        Collection<ClientData> membersOfGroup = null;
        Collection<ClientData> activeClientMembers = null;
        Collection<GroupRoleData> groupRoles = null;
        GroupRoleData selectedRole = null;
        Collection<CalendarData> calendars = null;
        CalendarData collectionMeetingCalendar = null;

        if (!associationParameters.isEmpty()) {
            if (associationParameters.contains("all")) {
                associationParameters.addAll(Arrays.asList("clientMembers", "activeClientMembers",
                        "groupRoles", "calendars", "collectionMeetingCalendar"));
            }
            if (associationParameters.contains("clientMembers")) {
                membersOfGroup = this.clientReadPlatformService.retrieveClientMembersOfGroup(groupId);
                if (CollectionUtils.isEmpty(membersOfGroup)) {
                    membersOfGroup = null;
                }
            }
            if (associationParameters.contains("activeClientMembers")) {
                activeClientMembers = this.clientReadPlatformService.retrieveActiveClientMembersOfGroup(groupId);
                if (CollectionUtils.isEmpty(activeClientMembers)) {
                    activeClientMembers = null;
                }
            }
            if (associationParameters.contains("groupRoles")) {
                groupRoles = this.groupRolesReadPlatformService.retrieveGroupRoles(groupId);
                if (CollectionUtils.isEmpty(groupRoles)) {
                    groupRoles = null;
                }
            }
            if (associationParameters.contains("parentCalendars")) {
                final List<Integer> calendarTypeOptions = CalendarUtils.createIntegerListFromQueryParameter("all");
                calendars = this.calendarReadPlatformService.retrieveParentCalendarsByEntity(groupId, CalendarEntityType.GROUPS.getValue(),
                        calendarTypeOptions);
                if (CollectionUtils.isEmpty(calendars)) {
                    calendars = null;
                }
            }
            if (associationParameters.contains("collectionMeetingCalendar")) {
                if (group.isChildGroup()) {
                    collectionMeetingCalendar = this.calendarReadPlatformService.retrieveCollctionCalendarByEntity(group.getParentId(),
                            CalendarEntityType.CENTERS.getValue());
                } else {
                    collectionMeetingCalendar = this.calendarReadPlatformService.retrieveCollctionCalendarByEntity(groupId,
                            CalendarEntityType.GROUPS.getValue());
                }
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

            group = GroupGeneralData.withAssocations(group, membersOfGroup, activeClientMembers,
                    groupRoles, calendars, collectionMeetingCalendar);
        }

        if (roleId != null) {
            selectedRole = this.groupRolesReadPlatformService.retrieveGroupRole(groupId, roleId);
            if (selectedRole != null) {
                group = GroupGeneralData.updateSelectedRole(group, selectedRole);
            }
        }

        final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
        if (template) {
            final GroupGeneralData templateGroup = this.groupReadPlatformService.retrieveTemplate(group.officeId(), false,
                    staffInSelectedOfficeOnly);
            group = GroupGeneralData.withTemplate(templateGroup, group);
        }

        return this.groupGeneralApiJsonSerializer.serialize(settings, group, GroupingTypesApiConstants.GROUP_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String create(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createGroup() //
                .withJson(apiRequestBodyAsJson) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{groupId}/command/unassign_staff")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String unassignLoanOfficer(@PathParam("groupId") final Long groupId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .unassignGroupStaff(groupId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);

    }

    @PUT
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String update(@PathParam("groupId") final Long groupId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateGroup(groupId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String delete(@PathParam("groupId") final Long groupId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteGroup(groupId) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String activateOrGenerateCollectionSheet(@PathParam("groupId") final Long groupId,
            @QueryParam("command") final String commandParam, @QueryParam("roleId") final Long roleId, final String apiRequestBodyAsJson,
            @Context final UriInfo uriInfo) {
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;
        if (is(commandParam, "activate")) {
            final CommandWrapper commandRequest = builder.activateGroup(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "associateClients")) {
            final CommandWrapper commandRequest = builder.associateClientsToGroup(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "disassociateClients")) {
            final CommandWrapper commandRequest = builder.disassociateClientsFromGroup(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "generateCollectionSheet")) {
            final JsonElement parsedQuery = this.fromJsonHelper.parse(apiRequestBodyAsJson);
            final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, this.fromJsonHelper);
            final JLGCollectionSheetData collectionSheet = this.collectionSheetReadPlatformService.generateGroupCollectionSheet(groupId,
                    query);
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, collectionSheet, GroupingTypesApiConstants.COLLECTIONSHEET_DATA_PARAMETERS);
        } else if (is(commandParam, "saveCollectionSheet")) {
            final CommandWrapper commandRequest = builder.saveGroupCollectionSheet(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "unassignStaff")) {
            final CommandWrapper commandRequest = builder.unassignGroupStaff(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "assignStaff")) {
            final CommandWrapper commandRequest = builder.assignGroupStaff(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "assignRole")) {
            final CommandWrapper commandRequest = builder.assignRole(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "unassignRole")) {
            final CommandWrapper commandRequest = builder.unassignRole(groupId, roleId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "updateRole")) {
            final CommandWrapper commandRequest = builder.updateRole(groupId, roleId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "transferClients")) {
            final CommandWrapper commandRequest = builder.transferClientsBetweenGroups(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "close")) {
            final CommandWrapper commandRequest = builder.closeGroup(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "activate", "generateCollectionSheet",
                    "saveCollectionSheet", "unassignStaff", "assignRole", "unassignRole", "updateassignRole" });
        }

    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("{groupId}/accounts")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAccounts(@PathParam("groupId") final Long groupId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission("GROUP");

        final AccountSummaryCollectionData groupAccount = this.accountDetailsReadPlatformService.retrieveGroupAccountDetails(groupId);

        final Set<String> GROUP_ACCOUNTS_DATA_PARAMETERS = new HashSet<>(Arrays.asList("loanAccounts", "savingsAccounts",
                "memberLoanAccounts", "memberSavingsAccounts"));

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.groupSummaryToApiJsonSerializer.serialize(settings, groupAccount, GROUP_ACCOUNTS_DATA_PARAMETERS);
    }
}