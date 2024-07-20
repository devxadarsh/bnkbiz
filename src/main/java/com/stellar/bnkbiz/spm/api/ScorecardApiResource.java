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
package com.stellar.bnkbiz.spm.api;

import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
import com.stellar.bnkbiz.portfolio.client.domain.Client;
import com.stellar.bnkbiz.portfolio.client.domain.ClientRepository;
import com.stellar.bnkbiz.portfolio.client.exception.ClientNotFoundException;
import com.stellar.bnkbiz.spm.data.ScorecardData;
import com.stellar.bnkbiz.spm.domain.Scorecard;
import com.stellar.bnkbiz.spm.domain.Survey;
import com.stellar.bnkbiz.spm.exception.SurveyNotFoundException;
import com.stellar.bnkbiz.spm.service.ScorecardService;
import com.stellar.bnkbiz.spm.service.SpmService;
import com.stellar.bnkbiz.spm.util.ScorecardMapper;
import com.stellar.bnkbiz.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

@Path("/surveys/{surveyId}/scorecards")
@Component
@Scope("singleton")
public class ScorecardApiResource {

    private final PlatformSecurityContext securityContext;
    private final SpmService spmService;
    private final ScorecardService scorecardService;
    private final ClientRepository clientRepository;

    @Autowired
    public ScorecardApiResource(final PlatformSecurityContext securityContext, final SpmService spmService,
                                final ScorecardService scorecardService, final ClientRepository clientRepository) {
        super();
        this.securityContext = securityContext;
        this.spmService = spmService;
        this.scorecardService = scorecardService;
        this.clientRepository = clientRepository;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public List<ScorecardData> findBySurvey(@PathParam("surveyId") final Long surveyId) {
        this.securityContext.authenticatedUser();

        final Survey survey = findSurvey(surveyId);

        final List<Scorecard> scorecards = this.scorecardService.findBySurvey(survey);

        if (scorecards == null) {
            return ScorecardMapper.map(scorecards);
        }

        return Collections.EMPTY_LIST;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public void createScorecard(@PathParam("surveyId") final Long surveyId, final ScorecardData scorecardData) {
        final AppUser appUser = this.securityContext.authenticatedUser();

        final Survey survey = findSurvey(surveyId);

        final Client client = this.clientRepository.findOne(scorecardData.getClientId());

        if (client == null) {
            throw new ClientNotFoundException(scorecardData.getClientId());
        }

        this.scorecardService.createScorecard(ScorecardMapper.map(scorecardData, survey, appUser, client));
    }

    @Path("/clients/{clientId}")
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public List<ScorecardData> findBySurveyClient(@PathParam("surveyId") final Long surveyId,
                                                  @PathParam("clientId") final Long clientId) {
        this.securityContext.authenticatedUser();

        final Survey survey = findSurvey(surveyId);

        final Client client = this.clientRepository.findOne(clientId);

        if (client == null) {
            throw new ClientNotFoundException(clientId);
        }

        final List<Scorecard> scorecards = this.scorecardService.findBySurveyAndClient(survey, client);

        if (scorecards == null) {
            return ScorecardMapper.map(scorecards);
        }

        return Collections.EMPTY_LIST;
    }

    private Survey findSurvey(final Long surveyId) {
        final Survey survey = this.spmService.findById(surveyId);
        if (survey == null) {
            throw new SurveyNotFoundException(surveyId);
        }
        return survey;
    }
}
