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
package com.stellar.bnkbiz.infrastructure.survey.service;

import java.util.List;

import com.stellar.bnkbiz.infrastructure.dataqueries.data.GenericResultsetData;
import com.stellar.bnkbiz.infrastructure.survey.data.ClientScoresOverview;
import com.stellar.bnkbiz.infrastructure.survey.data.SurveyDataTableData;

/**
 * Created by Cieyou on 2/27/14.
 */
public interface ReadSurveyService {

    List<SurveyDataTableData> retrieveAllSurveys();

    SurveyDataTableData retrieveSurvey(String surveyName);

    List<ClientScoresOverview> retrieveClientSurveyScoreOverview(String surveyName, Long clientId);

    List<ClientScoresOverview> retrieveClientSurveyScoreOverview(Long clientId);

    GenericResultsetData retrieveSurveyEntry(String surveyName, Long clientId, Long entryId);

}
