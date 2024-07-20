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

import java.util.Collection;
import java.util.Date;

import com.stellar.bnkbiz.infrastructure.core.data.PaginationParameters;
import com.stellar.bnkbiz.infrastructure.core.service.Page;
import com.stellar.bnkbiz.infrastructure.core.service.SearchParameters;
import com.stellar.bnkbiz.portfolio.group.data.CenterData;
import com.stellar.bnkbiz.portfolio.group.data.GroupGeneralData;
import com.stellar.bnkbiz.portfolio.group.data.StaffCenterData;

public interface CenterReadPlatformService {

    CenterData retrieveTemplate(Long officeId, boolean staffInSelectedOfficeOnly);

    CenterData retrieveOne(Long centerId);

    Collection<CenterData> retrieveAllForDropdown(Long officeId);

    Page<CenterData> retrievePagedAll(SearchParameters searchParameters, PaginationParameters parameters);

    Collection<CenterData> retrieveAll(SearchParameters searchParameters, PaginationParameters parameters);

    GroupGeneralData retrieveCenterGroupTemplate(Long centerId);

    Collection<GroupGeneralData> retrieveAssociatedGroups(Long centerId);

    CenterData retrieveCenterWithClosureReasons();

    Collection<StaffCenterData> retriveAllCentersByMeetingDate(Long officeId, Date meetingDate, Long staffId);
}
