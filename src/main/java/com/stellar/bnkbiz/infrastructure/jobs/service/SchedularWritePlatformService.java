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
package com.stellar.bnkbiz.infrastructure.jobs.service;

import java.util.List;

import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.jobs.domain.ScheduledJobDetail;
import com.stellar.bnkbiz.infrastructure.jobs.domain.ScheduledJobRunHistory;
import com.stellar.bnkbiz.infrastructure.jobs.domain.SchedulerDetail;

public interface SchedularWritePlatformService {

    public List<ScheduledJobDetail> retrieveAllJobs();

    public ScheduledJobDetail findByJobKey(String triggerKey);

    public void saveOrUpdate(ScheduledJobDetail scheduledJobDetails);

    public void saveOrUpdate(ScheduledJobDetail scheduledJobDetails, ScheduledJobRunHistory scheduledJobRunHistory);

    public Long fetchMaxVersionBy(String triggerKey);

    public ScheduledJobDetail findByJobId(Long jobId);

    public CommandProcessingResult updateJobDetail(Long jobId, JsonCommand command);

    public SchedulerDetail retriveSchedulerDetail();

    public void updateSchedulerDetail(final SchedulerDetail schedulerDetail);

    public boolean processJobDetailForExecution(String jobKey, String triggerType);

}
