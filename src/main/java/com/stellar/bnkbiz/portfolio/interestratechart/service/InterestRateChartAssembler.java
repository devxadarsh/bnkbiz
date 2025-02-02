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
package com.stellar.bnkbiz.portfolio.interestratechart.service;

import static com.stellar.bnkbiz.portfolio.interestratechart.InterestRateChartApiConstants.INTERESTRATE_CHART_RESOURCE_NAME;
import static com.stellar.bnkbiz.portfolio.interestratechart.InterestRateChartApiConstants.descriptionParamName;
import static com.stellar.bnkbiz.portfolio.interestratechart.InterestRateChartApiConstants.endDateParamName;
import static com.stellar.bnkbiz.portfolio.interestratechart.InterestRateChartApiConstants.fromDateParamName;
import static com.stellar.bnkbiz.portfolio.interestratechart.InterestRateChartApiConstants.nameParamName;
import static com.stellar.bnkbiz.portfolio.interestratechart.InterestRateChartSlabApiConstants.currencyCodeParamName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.ApiParameterError;
import com.stellar.bnkbiz.infrastructure.core.data.DataValidatorBuilder;
import com.stellar.bnkbiz.infrastructure.core.exception.PlatformApiDataValidationException;
import com.stellar.bnkbiz.infrastructure.core.serialization.FromJsonHelper;
import com.stellar.bnkbiz.portfolio.interestratechart.data.InterestRateChartRepositoryWrapper;
import com.stellar.bnkbiz.portfolio.interestratechart.domain.InterestRateChart;
import com.stellar.bnkbiz.portfolio.interestratechart.domain.InterestRateChartFields;
import com.stellar.bnkbiz.portfolio.interestratechart.domain.InterestRateChartSlab;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class InterestRateChartAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final InterestRateChartRepositoryWrapper interestRateChartRepositoryWrapper;
    private final InterestRateChartSlabAssembler chartSlabAssembler;

    @Autowired
    public InterestRateChartAssembler(final FromJsonHelper fromApiJsonHelper,
            final InterestRateChartRepositoryWrapper interestRateChartRepositoryWrapper,
            final InterestRateChartSlabAssembler chartSlabAssembler) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.interestRateChartRepositoryWrapper = interestRateChartRepositoryWrapper;
        this.chartSlabAssembler = chartSlabAssembler;
    }

    /**
     * Assembles a new {@link InterestRateChart} from JSON Slabs passed in
     * request
     */
    public InterestRateChart assembleFrom(final JsonCommand command) {

        final JsonElement element = command.parsedJson();
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        @SuppressWarnings("unused")
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(INTERESTRATE_CHART_RESOURCE_NAME);
        final String currencyCode = this.fromApiJsonHelper.extractStringNamed(currencyCodeParamName, element);
        final InterestRateChart newChart = this.assembleFrom(element, currencyCode);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
        return newChart;
    }

    public InterestRateChart assembleFrom(final JsonElement element, final String currencyCode) {

        final String name = this.fromApiJsonHelper.extractStringNamed(nameParamName, element);
        final String description = this.fromApiJsonHelper.extractStringNamed(descriptionParamName, element);
        final LocalDate fromDate = this.fromApiJsonHelper.extractLocalDateNamed(fromDateParamName, element);
        final LocalDate toDate = this.fromApiJsonHelper.extractLocalDateNamed(endDateParamName, element);
        

        // assemble chart Slabs
        final Collection<InterestRateChartSlab> newChartSlabs = this.chartSlabAssembler.assembleChartSlabsFrom(element,
                currencyCode);

        final InterestRateChartFields fields = InterestRateChartFields.createNew(name, description, fromDate, toDate);
        final InterestRateChart newChart = InterestRateChart.createNew(fields, newChartSlabs);
        return newChart;
    }

    public InterestRateChart assembleFrom(final Long interestRateChartId) {
        final InterestRateChart interestRateChart = this.interestRateChartRepositoryWrapper
                .findOneWithNotFoundDetection(interestRateChartId);
        return interestRateChart;
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}