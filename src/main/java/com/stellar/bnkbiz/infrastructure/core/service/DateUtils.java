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
package com.stellar.bnkbiz.infrastructure.core.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
//import java.time.LocalDate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.stellar.bnkbiz.infrastructure.core.data.ApiParameterError;
import com.stellar.bnkbiz.infrastructure.core.domain.FineractPlatformTenant;
import com.stellar.bnkbiz.infrastructure.core.exception.PlatformApiDataValidationException;
//import org.joda.time.DateTime;
//import org.joda.time.DateTimeZone;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import org.joda.time.format.DateTimeFormat;
//import org.joda.time.format.DateTimeFormatter;
import java.time.ZoneId;

public class DateUtils {

    public static ZoneId getDateTimeZoneOfTenant() {
        final FineractPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        ZoneId zone = null;
        if (tenant != null) {
            zone = ZoneId.forID(tenant.getTimezoneId());
            TimeZone.getTimeZone(tenant.getTimezoneId());
        }
        return zone;
    }

    public static TimeZone getTimeZoneOfTenant() {
        final FineractPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        TimeZone zone = null;
        if (tenant != null) {
            zone = TimeZone.getTimeZone(tenant.getTimezoneId());
        }
        return zone;
    }

    public static Date getDateOfTenant() {
        return getLocalDateOfTenant().toDateTimeAtStartOfDay().toDate();
    }

    public static LocalDate getLocalDateOfTenant() {

        LocalDate today = new LocalDate();

        final ZoneId zone = getDateTimeZoneOfTenant();
        if (zone != null) {
            today = new LocalDate(zone);
        }

        return today;
    }

    public static LocalDateTime getLocalDateTimeOfTenant() {

        LocalDateTime today = new LocalDateTime();

        final ZoneId zone = getDateTimeZoneOfTenant();
        if (zone != null) {
            today = new LocalDateTime(zone);
        }

        return today;
    }

    public static LocalDate parseLocalDate(final String stringDate, final String pattern) {

        try {
            final DateTimeFormatter dateStringFormat = DateTimeFormatter.ofPattern(pattern);
            dateStringFormat.withZone(getDateTimeZoneOfTenant());
            final DateTime dateTime = dateStringFormat.parseDateTime(stringDate);
            return dateTime.toLocalDate();
        } catch (final IllegalArgumentException e) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.date.pattern", "The parameter date ("
                    + stringDate + ") is invalid w.r.t. pattern " + pattern, "date", stringDate, pattern);
            dataValidationErrors.add(error);
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public static String formatToSqlDate(final Date date) {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        df.setTimeZone(getTimeZoneOfTenant());
        final String formattedSqlDate = df.format(date);
        return formattedSqlDate;
    }

    public static boolean isDateInTheFuture(final LocalDate localDate) {
        return localDate.isAfter(getLocalDateOfTenant());
    }
}