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
package com.stellar.bnkbiz.spm.service;

import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
import com.stellar.bnkbiz.spm.domain.Survey;
import com.stellar.bnkbiz.spm.repository.SurveyRepository;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class SpmService {

    private final PlatformSecurityContext securityContext;
    private final SurveyRepository surveyRepository;

    @Autowired
    public SpmService(final PlatformSecurityContext securityContext,
                      final SurveyRepository surveyRepository) {
        super();
        this.securityContext = securityContext;
        this.surveyRepository = surveyRepository;
    }

    public List<Survey> fetchValidSurveys() {
        this.securityContext.authenticatedUser();

        return this.surveyRepository.fetchActiveSurveys(new Date());
    }

    public Survey findById(final Long id) {
        this.securityContext.authenticatedUser();

        return this.surveyRepository.findOne(id);
    }

    public Survey createSurvey(final Survey survey) {
        this.securityContext.authenticatedUser();

        final Survey previousSurvey = this.surveyRepository.findByKey(survey.getKey(), new Date());

        if (previousSurvey != null) {
            this.deactivateSurvey(previousSurvey.getId());
        }

        // set valid from to start of today
        final DateTime validFrom = DateTime
                .now()
                .withHourOfDay(0)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        survey.setValidFrom(validFrom.toDate());

        // set valid from to end in 100 years
        final DateTime validTo = validFrom
                .withDayOfMonth(31)
                .withMonthOfYear(12)
                .withHourOfDay(23)
                .withMinuteOfHour(59)
                .withSecondOfMinute(59)
                .withMillisOfSecond(999)
                .plusYears(100);

        survey.setValidTo(validTo.toDate());

        return this.surveyRepository.save(survey);
    }

    public void deactivateSurvey(final Long id) {
        this.securityContext.authenticatedUser();

        final Survey survey = this.surveyRepository.findOne(id);

        if (survey != null) {
            // set valid to to yesterday night
            final DateTime dateTime = DateTime
                    .now()
                    .withHourOfDay(23)
                    .withMinuteOfHour(59)
                    .withSecondOfMinute(59)
                    .withMillisOfSecond(999)
                    .minusDays(1);
            survey.setValidTo(dateTime.toDate());

            this.surveyRepository.save(survey);
        }
    }
}
