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
package com.stellar.bnkbiz.infrastructure.jobs.domain;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.jobs.api.SchedulerJobApiConstants;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "job")
public class ScheduledJobDetail extends AbstractPersistable<Long> {

    @Column(name = "name")
    private String jobName;

    @Column(name = "display_name")
    private String jobDisplayName;

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "task_priority")
    private Short taskPriority;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "previous_run_start_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date previousRunStartTime;

    @Column(name = "next_run_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextRunTime;

    @Column(name = "job_key")
    private String jobKey;

    @Column(name = "initializing_errorlog")
    private String errorLog;

    @Column(name = "is_active")
    private boolean activeSchedular;

    @Column(name = "currently_running")
    private boolean currentlyRunning;

    @Column(name = "updates_allowed")
    private boolean updatesAllowed;

    @Column(name = "scheduler_group")
    private Short schedulerGroup;

    @Column(name = "is_misfired")
    private boolean triggerMisfired;

    protected ScheduledJobDetail() {

    }

    public String getJobName() {
        return this.jobName;
    }

    public String getCronExpression() {
        return this.cronExpression;
    }

    public Short getTaskPriority() {
        return this.taskPriority;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public String getJobKey() {
        return this.jobKey;
    }

    public Short getSchedulerGroup() {
        return this.schedulerGroup;
    }

    public boolean isActiveSchedular() {
        return this.activeSchedular;
    }

    public void updateCronExpression(final String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public void updatePreviousRunStartTime(final Date previousRunStartTime) {
        this.previousRunStartTime = previousRunStartTime;
    }

    public Date getNextRunTime() {
        return this.nextRunTime;
    }

    public void updateNextRunTime(final Date nextRunTime) {
        this.nextRunTime = nextRunTime;
    }

    public void updateJobKey(final String jobKey) {
        this.jobKey = jobKey;
    }

    public void updateErrorLog(final String errorLog) {
        this.errorLog = errorLog;
    }

    public boolean isCurrentlyRunning() {
        return this.currentlyRunning;
    }

    public void updateCurrentlyRunningStatus(final boolean currentlyRunning) {
        this.currentlyRunning = currentlyRunning;
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        if (command.isChangeInStringParameterNamed(SchedulerJobApiConstants.displayNameParamName, this.jobDisplayName)) {
            final String newValue = command.stringValueOfParameterNamed(SchedulerJobApiConstants.displayNameParamName).trim();
            actualChanges.put(SchedulerJobApiConstants.displayNameParamName, newValue);
            this.jobDisplayName = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInStringParameterNamed(SchedulerJobApiConstants.cronExpressionParamName, this.cronExpression)) {
            final String newValue = command.stringValueOfParameterNamed(SchedulerJobApiConstants.cronExpressionParamName).trim();
            actualChanges.put(SchedulerJobApiConstants.cronExpressionParamName, newValue);
            this.cronExpression = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInBooleanParameterNamed(SchedulerJobApiConstants.jobActiveStatusParamName, this.activeSchedular)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(SchedulerJobApiConstants.jobActiveStatusParamName);
            actualChanges.put(SchedulerJobApiConstants.jobActiveStatusParamName, newValue);
            this.activeSchedular = newValue;
        }

        return actualChanges;
    }

    public boolean isTriggerMisfired() {
        return this.triggerMisfired;
    }

    public void updateTriggerMisfired(final boolean triggerMisfired) {
        this.triggerMisfired = triggerMisfired;
    }

}
