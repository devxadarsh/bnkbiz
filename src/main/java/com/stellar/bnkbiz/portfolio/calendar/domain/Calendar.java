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
package com.stellar.bnkbiz.portfolio.calendar.domain;

import static com.stellar.bnkbiz.portfolio.calendar.CalendarConstants.CALENDAR_RESOURCE_NAME;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.ApiParameterError;
import com.stellar.bnkbiz.infrastructure.core.data.DataValidatorBuilder;
import com.stellar.bnkbiz.infrastructure.core.domain.AbstractAuditableCustom;
import com.stellar.bnkbiz.infrastructure.core.exception.PlatformApiDataValidationException;
import com.stellar.bnkbiz.infrastructure.core.service.DateUtils;
import com.stellar.bnkbiz.portfolio.calendar.CalendarConstants.CALENDAR_SUPPORTED_PARAMETERS;
import com.stellar.bnkbiz.portfolio.calendar.exception.CalendarDateException;
import com.stellar.bnkbiz.portfolio.calendar.exception.CalendarParameterUpdateNotSupportedException;
import com.stellar.bnkbiz.portfolio.calendar.service.CalendarUtils;
import com.stellar.bnkbiz.useradministration.domain.AppUser;
import java.time.LocalDate;

@Entity
@Table(name = "m_calendar")
public class Calendar extends AbstractAuditableCustom<AppUser, Long> {

    @Column(name = "title", length = 50, nullable = false)
    private String title;

    @Column(name = "description", length = 100, nullable = true)
    private String description;

    @Column(name = "location", length = 100, nullable = true)
    private String location;

    @Column(name = "start_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name = "duration", nullable = true)
    private Integer duration;

    @Column(name = "calendar_type_enum", nullable = false)
    private Integer typeId;

    @Column(name = "repeating", nullable = false)
    private boolean repeating = false;

    @Column(name = "recurrence", length = 100, nullable = true)
    private String recurrence;

    @Column(name = "remind_by_enum", nullable = true)
    private Integer remindById;

    @Column(name = "first_reminder", nullable = true)
    private Integer firstReminder;

    @Column(name = "second_reminder", nullable = true)
    private Integer secondReminder;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "calendar_id")
    private final Set<CalendarHistory> calendarHistory = new HashSet<>();

    protected Calendar() {

    }

    public Calendar(final String title, final String description, final String location, final LocalDate startDate,
            final LocalDate endDate, final Integer duration, final Integer typeId, final boolean repeating, final String recurrence,
            final Integer remindById, final Integer firstReminder, final Integer secondReminder) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(CALENDAR_RESOURCE_NAME);

        final CalendarType calendarType = CalendarType.fromInt(typeId);
        if (calendarType.isCollection() && !repeating) {
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.REPEATING.getValue())
                    .failWithCodeNoParameterAddedToErrorCode("must.repeat.for.collection.calendar");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        this.title = StringUtils.defaultIfEmpty(title, null);
        this.description = StringUtils.defaultIfEmpty(description, null);
        this.location = StringUtils.defaultIfEmpty(location, null);

        if (null != startDate) {
            this.startDate = startDate.toDateTimeAtStartOfDay().toDate();
        } else {
            this.startDate = null;
        }

        if (null != endDate) {
            this.endDate = endDate.toDateTimeAtStartOfDay().toDate();
        } else {
            this.endDate = null;
        }

        this.duration = duration;
        this.typeId = typeId;
        this.repeating = repeating;
        this.recurrence = StringUtils.defaultIfEmpty(recurrence, null);
        this.remindById = remindById;
        this.firstReminder = firstReminder;
        this.secondReminder = secondReminder;
    }

    public static Calendar createRepeatingCalendar(final String title, final LocalDate startDate, final Integer typeId,
            final CalendarFrequencyType frequencyType, final Integer interval, final Integer repeatsOnDay) {

        final String description = null;
        final String location = null;
        final LocalDate endDate = null;
        final Integer duration = null;
        final boolean repeating = true;
        final Integer remindById = null;
        final Integer firstReminder = null;
        final Integer secondReminder = null;
        final String recurrence = constructRecurrence(frequencyType, interval, repeatsOnDay);

        return new Calendar(title, description, location, startDate, endDate, duration, typeId, repeating, recurrence, remindById,
                firstReminder, secondReminder);
    }

    public static Calendar fromJson(final JsonCommand command) {

        // final Long entityId = command.getSupportedEntityId();
        // final Integer entityTypeId =
        // CalendarEntityType.valueOf(command.getSupportedEntityType().toUpperCase()).getValue();
        final String title = command.stringValueOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.TITLE.getValue());
        final String description = command.stringValueOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.DESCRIPTION.getValue());
        final String location = command.stringValueOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.LOCATION.getValue());
        final LocalDate startDate = command.localDateValueOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.START_DATE.getValue());
        final LocalDate endDate = command.localDateValueOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.END_DATE.getValue());
        final Integer duration = command.integerValueSansLocaleOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.DURATION.getValue());
        final Integer typeId = command.integerValueSansLocaleOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.TYPE_ID.getValue());
        final boolean repeating = command.booleanPrimitiveValueOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.REPEATING.getValue());
        final Integer remindById = command.integerValueSansLocaleOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.REMIND_BY_ID.getValue());
        final Integer firstReminder = command.integerValueSansLocaleOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.FIRST_REMINDER
                .getValue());
        final Integer secondReminder = command.integerValueSansLocaleOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.SECOND_REMINDER
                .getValue());
        final String recurrence = Calendar.constructRecurrence(command, null);

        return new Calendar(title, description, location, startDate, endDate, duration, typeId, repeating, recurrence, remindById,
                firstReminder, secondReminder);
    }

    public Map<String, Object> updateStartDateAndDerivedFeilds(final LocalDate newMeetingStartDate) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        final LocalDate currentDate = DateUtils.getLocalDateOfTenant();

        if (newMeetingStartDate.isBefore(currentDate)) {
            final String defaultUserMessage = "New meeting effective from date cannot be in past";
            throw new CalendarDateException("new.start.date.cannot.be.in.past", defaultUserMessage, newMeetingStartDate,
                    getStartDateLocalDate());
        } else if (isStartDateAfter(newMeetingStartDate) && isStartDateBeforeOrEqual(currentDate)) {
            // new meeting date should be on or after start date or current
            // date
            final String defaultUserMessage = "New meeting effective from date cannot be a date before existing meeting start date";
            throw new CalendarDateException("new.start.date.before.existing.date", defaultUserMessage, newMeetingStartDate,
                    getStartDateLocalDate());
        } else {

            actualChanges.put(CALENDAR_SUPPORTED_PARAMETERS.START_DATE.getValue(), newMeetingStartDate.toString());
            this.startDate = newMeetingStartDate.toDate();

            /*
             * If meeting start date is changed then there is possibilities of
             * recurring day may change, so derive the recurring day and update
             * it if it is changed. For weekly type is weekday and for monthly
             * type it is day of the month
             */

            CalendarFrequencyType calendarFrequencyType = CalendarUtils.getFrequency(this.recurrence);
            Integer interval = CalendarUtils.getInterval(this.recurrence);
            Integer repeatsOnDay = null;

            /*
             * Repeats on day, need to derive based on the start date
             */

            if (calendarFrequencyType.isWeekly()) {
                repeatsOnDay = newMeetingStartDate.getDayOfWeek();
            } else if (calendarFrequencyType.isMonthly()) {
                repeatsOnDay = newMeetingStartDate.getDayOfMonth();
            }

            // TODO cover other recurrence also

            this.recurrence = constructRecurrence(calendarFrequencyType, interval, repeatsOnDay);

        }

        return actualChanges;

    }

    public Map<String, Object> update(final JsonCommand command, final Boolean areActiveEntitiesSynced ) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        if (command.isChangeInStringParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.TITLE.getValue(), this.title)) {
            final String newValue = command.stringValueOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.TITLE.getValue());
            actualChanges.put(CALENDAR_SUPPORTED_PARAMETERS.TITLE.getValue(), newValue);
            this.title = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInStringParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.DESCRIPTION.getValue(), this.description)) {
            final String newValue = command.stringValueOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.DESCRIPTION.getValue());
            actualChanges.put(CALENDAR_SUPPORTED_PARAMETERS.DESCRIPTION.getValue(), newValue);
            this.description = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInStringParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.LOCATION.getValue(), this.location)) {
            final String newValue = command.stringValueOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.LOCATION.getValue());
            actualChanges.put(CALENDAR_SUPPORTED_PARAMETERS.LOCATION.getValue(), newValue);
            this.location = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();
        final String startDateParamName = CALENDAR_SUPPORTED_PARAMETERS.START_DATE.getValue();
        if (command.isChangeInLocalDateParameterNamed(startDateParamName, getStartDateLocalDate())) {

            final String valueAsInput = command.stringValueOfParameterNamed(startDateParamName);
            final LocalDate newValue = command.localDateValueOfParameterNamed(startDateParamName);
            final LocalDate currentDate = DateUtils.getLocalDateOfTenant();

            if (newValue.isBefore(currentDate)) {
                final String defaultUserMessage = "New meeting effective from date cannot be in past";
                throw new CalendarDateException("new.start.date.cannot.be.in.past", defaultUserMessage, newValue, getStartDateLocalDate());
            } else if (isStartDateAfter(newValue) && isStartDateBeforeOrEqual(currentDate)) {
                // new meeting date should be on or after start date or current
                // date
                final String defaultUserMessage = "New meeting effective from date cannot be a date before existing meeting start date";
                throw new CalendarDateException("new.start.date.before.existing.date", defaultUserMessage, newValue,
                        getStartDateLocalDate());
            } else {
                actualChanges.put(startDateParamName, valueAsInput);
                actualChanges.put("dateFormat", dateFormatAsInput);
                actualChanges.put("locale", localeAsInput);
                this.startDate = newValue.toDate();
            }
        }

        final String endDateParamName = CALENDAR_SUPPORTED_PARAMETERS.END_DATE.getValue();
        if (command.isChangeInLocalDateParameterNamed(endDateParamName, getEndDateLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(endDateParamName);
            actualChanges.put(endDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(endDateParamName);
            this.endDate = newValue.toDate();
        }

        final String durationParamName = CALENDAR_SUPPORTED_PARAMETERS.DURATION.getValue();
        if (command.isChangeInIntegerSansLocaleParameterNamed(durationParamName, this.duration)) {
            final Integer newValue = command.integerValueSansLocaleOfParameterNamed(durationParamName);
            actualChanges.put(durationParamName, newValue);
            this.duration = newValue;
        }

        // Do not allow to change calendar type
        // TODO: AA Instead of throwing an exception, do not allow meeting
        // calendar type to update.
        final String typeParamName = CALENDAR_SUPPORTED_PARAMETERS.TYPE_ID.getValue();
        if (command.isChangeInIntegerSansLocaleParameterNamed(typeParamName, this.typeId)) {
            final Integer newValue = command.integerValueSansLocaleOfParameterNamed(typeParamName);
            final String defaultUserMessage = "Meeting calendar type update is not supported";
            final String oldMeeingType = CalendarType.fromInt(this.typeId).name();
            final String newMeetingType = CalendarType.fromInt(newValue).name();

            throw new CalendarParameterUpdateNotSupportedException("meeting.type", defaultUserMessage, newMeetingType, oldMeeingType);
            /*
             * final Integer newValue =
             * command.integerValueSansLocaleOfParameterNamed(typeParamName);
             * actualChanges.put(typeParamName, newValue); this.typeId =
             * newValue;
             */
        }

        final String repeatingParamName = CALENDAR_SUPPORTED_PARAMETERS.REPEATING.getValue();
        if (command.isChangeInBooleanParameterNamed(repeatingParamName, this.repeating)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(repeatingParamName);
            actualChanges.put(repeatingParamName, newValue);
            this.repeating = newValue;
        }

        // if repeating is false then update recurrence to NULL
        if (!this.repeating) this.recurrence = null;

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(CALENDAR_RESOURCE_NAME);

        final CalendarType calendarType = CalendarType.fromInt(this.typeId);
        if (calendarType.isCollection() && !this.repeating) {
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.REPEATING.getValue())
                    .failWithCodeNoParameterAddedToErrorCode("must.repeat.for.collection.calendar");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        final String newRecurrence = Calendar.constructRecurrence(command, this);
        if (!StringUtils.isBlank(this.recurrence) && !newRecurrence.equalsIgnoreCase(this.recurrence)) {
            /*
             * If active entities like JLG loan or RD accounts are synced to the
             * calendar then do not allow to change meeting frequency
             */

            if ( areActiveEntitiesSynced && !CalendarUtils.isFrequencySame(this.recurrence, newRecurrence)) {
                final String defaultUserMessage = "Update of meeting frequency is not supported";
                throw new CalendarParameterUpdateNotSupportedException("meeting.frequency", defaultUserMessage);
            }

            /*
             * If active entities like JLG loan or RD accounts are synced to the
             * calendar then do not allow to change meeting interval
             */

            if (areActiveEntitiesSynced && !CalendarUtils.isIntervalSame(this.recurrence, newRecurrence)) {
                final String defaultUserMessage = "Update of meeting interval is not supported";
                throw new CalendarParameterUpdateNotSupportedException("meeting.interval", defaultUserMessage);
            }

            actualChanges.put("recurrence", newRecurrence);
            this.recurrence = StringUtils.defaultIfEmpty(newRecurrence, null);
        }

        final String remindByParamName = CALENDAR_SUPPORTED_PARAMETERS.REMIND_BY_ID.getValue();
        if (command.isChangeInIntegerSansLocaleParameterNamed(remindByParamName, this.remindById)) {
            final Integer newValue = command.integerValueSansLocaleOfParameterNamed(remindByParamName);
            actualChanges.put(remindByParamName, newValue);
            this.remindById = newValue;
        }

        final String firstRemindarParamName = CALENDAR_SUPPORTED_PARAMETERS.FIRST_REMINDER.getValue();
        if (command.isChangeInIntegerSansLocaleParameterNamed(firstRemindarParamName, this.firstReminder)) {
            final Integer newValue = command.integerValueSansLocaleOfParameterNamed(firstRemindarParamName);
            actualChanges.put(firstRemindarParamName, newValue);
            this.firstReminder = newValue;
        }

        final String secondRemindarParamName = CALENDAR_SUPPORTED_PARAMETERS.SECOND_REMINDER.getValue();
        if (command.isChangeInIntegerSansLocaleParameterNamed(secondRemindarParamName, this.secondReminder)) {
            final Integer newValue = command.integerValueSansLocaleOfParameterNamed(secondRemindarParamName);
            actualChanges.put(secondRemindarParamName, newValue);
            this.secondReminder = newValue;
        }

        return actualChanges;
    }

    @SuppressWarnings("null")
    public Map<String, Object> updateRepeatingCalendar(final LocalDate calendarStartDate, final CalendarFrequencyType frequencyType,
            final Integer interval, final Integer repeatsOnDay) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        if (calendarStartDate != null & this.startDate != null) {
            if (!calendarStartDate.equals(this.getStartDateLocalDate())) {
                actualChanges.put("startDate", calendarStartDate);
                this.startDate = calendarStartDate.toDate();
            }
        }

        final String newRecurrence = Calendar.constructRecurrence(frequencyType, interval, repeatsOnDay);
        if (!StringUtils.isBlank(this.recurrence) && !newRecurrence.equalsIgnoreCase(this.recurrence)) {
            actualChanges.put("recurrence", newRecurrence);
            this.recurrence = newRecurrence;
        }
        return actualChanges;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getLocation() {
        return this.location;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public Integer getDuration() {
        return this.duration;
    }

    public Integer getTypeId() {
        return this.typeId;
    }

    public boolean isRepeating() {
        return this.repeating;
    }

    public String getRecurrence() {
        return this.recurrence;
    }

    public Integer getRemindById() {
        return this.remindById;
    }

    public Integer getFirstReminder() {
        return this.firstReminder;
    }

    public Integer getSecondReminder() {
        return this.secondReminder;
    }

    public LocalDate getStartDateLocalDate() {
        LocalDate startDateLocalDate = null;
        if (this.startDate != null) {
            startDateLocalDate = LocalDate.fromDateFields(this.startDate);
        }
        return startDateLocalDate;
    }

    public LocalDate getEndDateLocalDate() {
        LocalDate endDateLocalDate = null;
        if (this.endDate != null) {
            endDateLocalDate = LocalDate.fromDateFields(this.endDate);
        }
        return endDateLocalDate;
    }

    public Set<CalendarHistory> history() {
        return this.calendarHistory;
    }

    public boolean isStartDateBefore(final LocalDate compareDate) {
        if (this.startDate != null && compareDate != null && getStartDateLocalDate().isBefore(compareDate)) { return true; }
        return false;
    }

    public boolean isStartDateBeforeOrEqual(final LocalDate compareDate) {
        if (this.startDate != null && compareDate != null) {
            if (getStartDateLocalDate().isBefore(compareDate) || getStartDateLocalDate().equals(compareDate)) { return true; }
        }
        return false;
    }

    public boolean isStartDateAfter(final LocalDate compareDate) {
        if (this.startDate != null && compareDate != null && getStartDateLocalDate().isAfter(compareDate)) { return true; }
        return false;
    }

    public boolean isStartDateAfterOrEqual(final LocalDate compareDate) {
        if (this.startDate != null && compareDate != null) {
            if (getStartDateLocalDate().isAfter(compareDate) || getStartDateLocalDate().isEqual(compareDate)) { return true; }
        }
        return false;
    }

    public boolean isEndDateAfterOrEqual(final LocalDate compareDate) {
        if (this.endDate != null && compareDate != null) {
            if (getEndDateLocalDate().isAfter(compareDate) || getEndDateLocalDate().isEqual(compareDate)) { return true; }
        }
        return false;
    }

    public boolean isBetweenStartAndEndDate(final LocalDate compareDate) {
        if (isStartDateBeforeOrEqual(compareDate)) {
            if (getEndDateLocalDate() == null || isEndDateAfterOrEqual(compareDate)) { return true; }
        }
        return false;
    }

    private static String constructRecurrence(final JsonCommand command, final Calendar calendar) {
        final boolean repeating;
        if (command.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.REPEATING.getValue())) {
            repeating = command.booleanPrimitiveValueOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.REPEATING.getValue());
        } else if (calendar != null) {
            repeating = calendar.isRepeating();
        } else {
            repeating = false;
        }

        if (repeating) {
            final Integer frequency = command.integerValueOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.FREQUENCY.getValue());
            final CalendarFrequencyType frequencyType = CalendarFrequencyType.fromInt(frequency);
            final Integer interval = command.integerValueOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.INTERVAL.getValue());
            Integer repeatsOnDay = null;
            if (frequencyType.isWeekly()) {
                repeatsOnDay = command.integerValueOfParameterNamed(CALENDAR_SUPPORTED_PARAMETERS.REPEATS_ON_DAY.getValue());
            }

            return constructRecurrence(frequencyType, interval, repeatsOnDay);
        }
        return "";
    }

    private static String constructRecurrence(final CalendarFrequencyType frequencyType, final Integer interval, final Integer repeatsOnDay) {
        final StringBuilder recurrenceBuilder = new StringBuilder(200);

        recurrenceBuilder.append("FREQ=");
        recurrenceBuilder.append(frequencyType.toString().toUpperCase());
        if (interval > 1) {
            recurrenceBuilder.append(";INTERVAL=");
            recurrenceBuilder.append(interval);
        }
        if (frequencyType.isWeekly()) {
            final CalendarWeekDaysType weekDays = CalendarWeekDaysType.fromInt(repeatsOnDay);
            if (!weekDays.isInvalid()) {
                recurrenceBuilder.append(";BYDAY=");
                recurrenceBuilder.append(weekDays.toString().toUpperCase());
            }
        }
        return recurrenceBuilder.toString();
    }

    public boolean isValidRecurringDate(final LocalDate compareDate) {

        if (isBetweenStartAndEndDate(compareDate)) { return CalendarUtils.isValidRedurringDate(getRecurrence(), getStartDateLocalDate(),
                compareDate); }

        // validate with history details.
        for (CalendarHistory history : history()) {
            if (history.isBetweenStartAndEndDate(compareDate)) { return CalendarUtils.isValidRedurringDate(history.getRecurrence(),
                    history.getStartDateLocalDate(), compareDate); }
        }

        return false;
    }

    public void updateStartAndEndDate(final LocalDate startDate, final LocalDate endDate) {

        final CalendarFrequencyType frequencyType = CalendarUtils.getFrequency(this.recurrence);
        final Integer interval = new Integer(CalendarUtils.getInterval(this.recurrence));
        final String newRecurrence = Calendar.constructRecurrence(frequencyType, interval, startDate.getDayOfWeek());

        this.recurrence = newRecurrence;
        this.startDate = startDate.toDate();
        this.endDate = endDate.toDate();
    }
}
