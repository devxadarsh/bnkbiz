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
package com.stellar.bnkbiz.portfolio.group.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import com.stellar.bnkbiz.infrastructure.codes.data.CodeValueData;
import com.stellar.bnkbiz.infrastructure.codes.service.CodeValueReadPlatformService;
import com.stellar.bnkbiz.infrastructure.core.api.ApiParameterHelper;
import com.stellar.bnkbiz.infrastructure.core.data.PaginationParameters;
import com.stellar.bnkbiz.infrastructure.core.data.PaginationParametersDataValidator;
import com.stellar.bnkbiz.infrastructure.core.domain.JdbcSupport;
import com.stellar.bnkbiz.infrastructure.core.service.Page;
import com.stellar.bnkbiz.infrastructure.core.service.PaginationHelper;
import com.stellar.bnkbiz.infrastructure.core.service.RoutingDataSource;
import com.stellar.bnkbiz.infrastructure.core.service.SearchParameters;
import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
import com.stellar.bnkbiz.organisation.office.data.OfficeData;
import com.stellar.bnkbiz.organisation.office.service.OfficeReadPlatformService;
import com.stellar.bnkbiz.organisation.staff.data.StaffData;
import com.stellar.bnkbiz.organisation.staff.service.StaffReadPlatformService;
import com.stellar.bnkbiz.portfolio.client.data.ClientData;
import com.stellar.bnkbiz.portfolio.group.api.GroupingTypesApiConstants;
import com.stellar.bnkbiz.portfolio.group.data.CenterData;
import com.stellar.bnkbiz.portfolio.group.data.GroupGeneralData;
import com.stellar.bnkbiz.portfolio.group.domain.GroupTypes;
import com.stellar.bnkbiz.portfolio.group.exception.GroupNotFoundException;
import com.stellar.bnkbiz.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class GroupReadPlatformServiceImpl implements GroupReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final CenterReadPlatformService centerReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    private final AllGroupTypesDataMapper allGroupTypesDataMapper = new AllGroupTypesDataMapper();
    private final PaginationHelper<GroupGeneralData> paginationHelper = new PaginationHelper<>();
    private final PaginationParametersDataValidator paginationParametersDataValidator;

    private final static Set<String> supportedOrderByValues = new HashSet<>(Arrays.asList("id", "name", "officeId", "officeName"));

    @Autowired
    public GroupReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final CenterReadPlatformService centerReadPlatformService,
            final OfficeReadPlatformService officeReadPlatformService, final StaffReadPlatformService staffReadPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService,
            final PaginationParametersDataValidator paginationParametersDataValidator) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.centerReadPlatformService = centerReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.paginationParametersDataValidator = paginationParametersDataValidator;
    }

    @Override
    public GroupGeneralData retrieveTemplate(final Long officeId, final boolean isCenterGroup, final boolean staffInSelectedOfficeOnly) {

        final Long defaultOfficeId = defaultToUsersOfficeIfNull(officeId);

        Collection<CenterData> centerOptions = null;
        if (isCenterGroup) {
            centerOptions = this.centerReadPlatformService.retrieveAllForDropdown(defaultOfficeId);
        }

        final Collection<OfficeData> officeOptions = this.officeReadPlatformService.retrieveAllOfficesForDropdown();

        final boolean loanOfficersOnly = false;
        Collection<StaffData> staffOptions = null;
        if (staffInSelectedOfficeOnly) {
            staffOptions = this.staffReadPlatformService.retrieveAllStaffForDropdown(defaultOfficeId);
        } else {
            staffOptions = this.staffReadPlatformService.retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(defaultOfficeId,
                    loanOfficersOnly);
        }

        if (CollectionUtils.isEmpty(staffOptions)) {
            staffOptions = null;
        }

        final Collection<CodeValueData> availableRoles = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(GroupingTypesApiConstants.GROUP_ROLE_NAME);

        final Long centerId = null;
        final String accountNo = null;
        final String centerName = null;
        final Long staffId = null;
        final String staffName = null;
        final Collection<ClientData> clientOptions = null;
        
        return GroupGeneralData.template(defaultOfficeId, centerId, accountNo, centerName, staffId, staffName, centerOptions, officeOptions,
                staffOptions, clientOptions, availableRoles);
    }

    private Long defaultToUsersOfficeIfNull(final Long officeId) {
        Long defaultOfficeId = officeId;
        if (defaultOfficeId == null) {
            defaultOfficeId = this.context.authenticatedUser().getOffice().getId();
        }
        return defaultOfficeId;
    }

    @Override
    public Page<GroupGeneralData> retrievePagedAll(final SearchParameters searchParameters, final PaginationParameters parameters) {

        this.paginationParametersDataValidator.validateParameterValues(parameters, supportedOrderByValues, "audits");
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(this.allGroupTypesDataMapper.schema());
        sqlBuilder.append(" where o.hierarchy like ?");

        final String extraCriteria = getGroupExtraCriteria(searchParameters);

        if (StringUtils.isNotBlank(extraCriteria)) {
            sqlBuilder.append(" and (").append(extraCriteria).append(")");
        }

        if (parameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy()).append(' ').append(searchParameters.getSortOrder());
        }

        if (parameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(),
                new Object[] { hierarchySearchString }, this.allGroupTypesDataMapper);
    }

    @Override
    public Collection<GroupGeneralData> retrieveAll(SearchParameters searchParameters, final PaginationParameters parameters) {
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(this.allGroupTypesDataMapper.schema());
        sqlBuilder.append(" where o.hierarchy like ?");

        final String extraCriteria = getGroupExtraCriteria(searchParameters);

        if (StringUtils.isNotBlank(extraCriteria)) {
            sqlBuilder.append(" and (").append(extraCriteria).append(")");
        }

        if (parameters.isOrderByRequested()) {
            sqlBuilder.append(parameters.orderBySql());
        }

        if (parameters.isLimited()) {
            sqlBuilder.append(parameters.limitSql());
        }

        return this.jdbcTemplate.query(sqlBuilder.toString(), this.allGroupTypesDataMapper, new Object[] { hierarchySearchString });
    }

    // 'g.' preffix because of ERROR 1052 (23000): Column 'column_name' in where
    // clause is ambiguous
    // caused by the same name of columns in m_office and m_group tables
    private String getGroupExtraCriteria(final SearchParameters searchCriteria) {

        StringBuffer extraCriteria = new StringBuffer(200);
        extraCriteria.append(" and g.level_Id = ").append(GroupTypes.GROUP.getId());
        String sqlSearch = searchCriteria.getSqlSearch();
        if (sqlSearch != null) {
            sqlSearch = sqlSearch.replaceAll(" display_name ", " g.display_name ");
            sqlSearch = sqlSearch.replaceAll("display_name ", "g.display_name ");
            extraCriteria.append(" and ( ").append(sqlSearch).append(") ");
        }

        final Long officeId = searchCriteria.getOfficeId();
        if (officeId != null) {
            extraCriteria.append(" and g.office_id = ").append(officeId);
        }

        final String externalId = searchCriteria.getExternalId();
        if (externalId != null) {
            extraCriteria.append(" and g.external_id = ").append(ApiParameterHelper.sqlEncodeString(externalId));
        }

        final String name = searchCriteria.getName();
        if (name != null) {
            extraCriteria.append(" and g.display_name like ").append(ApiParameterHelper.sqlEncodeString("%" + name + "%"));
        }

        final String hierarchy = searchCriteria.getHierarchy();
        if (hierarchy != null) {
            extraCriteria.append(" and o.hierarchy like ").append(ApiParameterHelper.sqlEncodeString(hierarchy + "%"));
        }

        if (searchCriteria.isStaffIdPassed()) {
            extraCriteria.append(" and g.staff_id = ").append(searchCriteria.getStaffId());
        }

        if (StringUtils.isNotBlank(extraCriteria.toString())) {
            extraCriteria.delete(0, 4);
        }

        final Long staffId = searchCriteria.getStaffId();
        if (staffId != null) {
            extraCriteria.append(" and g.staff_id = ").append(staffId);
        }
        
        if(searchCriteria.isOrphansOnly()){
        	extraCriteria.append(" and g.parent_id IS NULL");
        }

        return extraCriteria.toString();
    }

    @Override
    public GroupGeneralData retrieveOne(final Long groupId) {

        try {
            final AppUser currentUser = this.context.authenticatedUser();
            final String hierarchy = currentUser.getOffice().getHierarchy();
            final String hierarchySearchString = hierarchy + "%";

            final String sql = "select " + this.allGroupTypesDataMapper.schema() + " where g.id = ? and o.hierarchy like ?";
            return this.jdbcTemplate.queryForObject(sql, this.allGroupTypesDataMapper, new Object[] { groupId, hierarchySearchString });
        } catch (final EmptyResultDataAccessException e) {
            throw new GroupNotFoundException(groupId);
        }
    }

    @Override
    public Collection<GroupGeneralData> retrieveGroupsForLookup(final Long officeId) {
        this.context.authenticatedUser();
        final GroupLookupDataMapper rm = new GroupLookupDataMapper();
        final String sql = "Select " + rm.schema() + " and g.office_id=?";
        return this.jdbcTemplate.query(sql, rm, new Object[] { officeId });
    }

    private static final class GroupLookupDataMapper implements RowMapper<GroupGeneralData> {

        public final String schema() {
            return "g.id as id, g.account_no as accountNo, g.display_name as displayName from m_group g where g.level_id = 2 ";
        }

        @Override
        public GroupGeneralData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final String accountNo = rs.getString("accountNo");
            final String displayName = rs.getString("displayName");
            return GroupGeneralData.lookup(id, accountNo, displayName);
        }
    }

    @Override
    public GroupGeneralData retrieveGroupWithClosureReasons() {
        final List<CodeValueData> closureReasons = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(GroupingTypesApiConstants.GROUP_CLOSURE_REASON));
        return GroupGeneralData.withClosureReasons(closureReasons);
    }

}