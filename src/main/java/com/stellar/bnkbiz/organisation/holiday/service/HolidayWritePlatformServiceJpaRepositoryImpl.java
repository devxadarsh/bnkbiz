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
package com.stellar.bnkbiz.organisation.holiday.service;

import static com.stellar.bnkbiz.organisation.holiday.api.HolidayApiConstants.officesParamName;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResultBuilder;
import com.stellar.bnkbiz.infrastructure.core.exception.PlatformDataIntegrityException;
import com.stellar.bnkbiz.infrastructure.core.serialization.FromJsonHelper;
import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
import com.stellar.bnkbiz.organisation.holiday.api.HolidayApiConstants;
import com.stellar.bnkbiz.organisation.holiday.data.HolidayDataValidator;
import com.stellar.bnkbiz.organisation.holiday.domain.Holiday;
import com.stellar.bnkbiz.organisation.holiday.domain.HolidayRepositoryWrapper;
import com.stellar.bnkbiz.organisation.holiday.exception.HolidayDateException;
import com.stellar.bnkbiz.organisation.office.domain.Office;
import com.stellar.bnkbiz.organisation.office.domain.OfficeRepository;
import com.stellar.bnkbiz.organisation.office.exception.OfficeNotFoundException;
import com.stellar.bnkbiz.organisation.workingdays.domain.WorkingDays;
import com.stellar.bnkbiz.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import com.stellar.bnkbiz.organisation.workingdays.service.WorkingDaysUtil;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service
public class HolidayWritePlatformServiceJpaRepositoryImpl implements HolidayWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(HolidayWritePlatformServiceJpaRepositoryImpl.class);

    private final HolidayDataValidator fromApiJsonDeserializer;
    private final HolidayRepositoryWrapper holidayRepository;
    private final WorkingDaysRepositoryWrapper daysRepositoryWrapper;
    private final PlatformSecurityContext context;
    private final OfficeRepository officeRepository;
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public HolidayWritePlatformServiceJpaRepositoryImpl(final HolidayDataValidator fromApiJsonDeserializer,
            final HolidayRepositoryWrapper holidayRepository, final PlatformSecurityContext context,
            final OfficeRepository officeRepository, final FromJsonHelper fromApiJsonHelper,
            final WorkingDaysRepositoryWrapper daysRepositoryWrapper) {
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.holidayRepository = holidayRepository;
        this.context = context;
        this.officeRepository = officeRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.daysRepositoryWrapper = daysRepositoryWrapper;
    }

    @Transactional
    @Override
    public CommandProcessingResult createHoliday(final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForCreate(command.json());

            validateInputDates(command);

            final Set<Office> offices = getSelectedOffices(command);

            final Holiday holiday = Holiday.createNew(offices, command);

            this.holidayRepository.save(holiday);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(holiday.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateHoliday(final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Holiday holiday = this.holidayRepository.findOneWithNotFoundDetection(command.entityId());
            Map<String, Object> changes = holiday.update(command);

            validateInputDates(holiday.getFromDateLocalDate(), holiday.getToDateLocalDate(), holiday.getRepaymentsRescheduledToLocalDate());

            if (changes.containsKey(officesParamName)) {
                final Set<Office> offices = getSelectedOffices(command);
                final boolean updated = holiday.update(offices);
                if (!updated) {
                    changes.remove(officesParamName);
                }
            }

            this.holidayRepository.saveAndFlush(holiday);

            return new CommandProcessingResultBuilder().withEntityId(holiday.getId()).with(changes).build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult activateHoliday(final Long holidayId) {
        this.context.authenticatedUser();
        final Holiday holiday = this.holidayRepository.findOneWithNotFoundDetection(holidayId);

        holiday.activate();
        this.holidayRepository.saveAndFlush(holiday);
        return new CommandProcessingResultBuilder().withEntityId(holiday.getId()).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteHoliday(final Long holidayId) {
        this.context.authenticatedUser();
        final Holiday holiday = this.holidayRepository.findOneWithNotFoundDetection(holidayId);
        holiday.delete();
        this.holidayRepository.saveAndFlush(holiday);
        return new CommandProcessingResultBuilder().withEntityId(holidayId).build();
    }

    private Set<Office> getSelectedOffices(final JsonCommand command) {
        Set<Office> offices = null;
        final JsonObject topLevelJsonElement = this.fromApiJsonHelper.parse(command.json()).getAsJsonObject();
        if (topLevelJsonElement.has(HolidayApiConstants.officesParamName)
                && topLevelJsonElement.get(HolidayApiConstants.officesParamName).isJsonArray()) {

            final JsonArray array = topLevelJsonElement.get(HolidayApiConstants.officesParamName).getAsJsonArray();
            offices = new HashSet<>(array.size());
            for (int i = 0; i < array.size(); i++) {
                final JsonObject officeElement = array.get(i).getAsJsonObject();
                final Long officeId = this.fromApiJsonHelper.extractLongNamed(HolidayApiConstants.officeIdParamName, officeElement);
                final Office office = this.officeRepository.findOne(officeId);
                if (office == null) { throw new OfficeNotFoundException(officeId); }
                offices.add(office);
            }
        }
        return offices;
    }

    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("holiday_name")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.holiday.duplicate.name", "Holiday with name `" + name + "` already exists",
                    "name", name);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.office.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void validateInputDates(final JsonCommand command) {
        final LocalDate fromDate = command.localDateValueOfParameterNamed(HolidayApiConstants.fromDateParamName);
        final LocalDate toDate = command.localDateValueOfParameterNamed(HolidayApiConstants.toDateParamName);
        final LocalDate repaymentsRescheduledTo = command
                .localDateValueOfParameterNamed(HolidayApiConstants.repaymentsRescheduledToParamName);
        this.validateInputDates(fromDate, toDate, repaymentsRescheduledTo);
    }

    private void validateInputDates(final LocalDate fromDate, final LocalDate toDate, final LocalDate repaymentsRescheduledTo) {

        String defaultUserMessage = "";

        if (toDate.isBefore(fromDate)) {
            defaultUserMessage = "To Date date cannot be before the From Date.";
            throw new HolidayDateException("to.date.cannot.be.before.from.date", defaultUserMessage, fromDate.toString(),
                    toDate.toString());
        }

        if (repaymentsRescheduledTo.isEqual(fromDate) || repaymentsRescheduledTo.isEqual(toDate)
                || (repaymentsRescheduledTo.isAfter(fromDate) && repaymentsRescheduledTo.isBefore(toDate))) {

            defaultUserMessage = "Repayments rescheduled date should be before from date or after to date.";
            throw new HolidayDateException("repayments.rescheduled.date.should.be.before.from.date.or.after.to.date", defaultUserMessage,
                    repaymentsRescheduledTo.toString());
        }

        final WorkingDays workingDays = this.daysRepositoryWrapper.findOne();
        final Boolean isRepaymentOnWorkingDay = WorkingDaysUtil.isWorkingDay(workingDays, repaymentsRescheduledTo);

        if (!isRepaymentOnWorkingDay) {
            defaultUserMessage = "Repayments rescheduled date should not fall on non working days";
            throw new HolidayDateException("repayments.rescheduled.date.should.not.fall.on.non.working.day", defaultUserMessage,
                    repaymentsRescheduledTo.toString());
        }

        // validate repaymentsRescheduledTo date
        // 1. should be within a 7 days date range.
        // 2. Alternative date should not be an exist holiday.//TBD
        // 3. Holiday should not be on an repaymentsRescheduledTo date of
        // another holiday.//TBD

        // restricting repaymentsRescheduledTo date to be within 7 days range
        // before or after from date and to date.
        if (repaymentsRescheduledTo.isBefore(fromDate.minusDays(7)) || repaymentsRescheduledTo.isAfter(toDate.plusDays(7))) {
            defaultUserMessage = "Repayments Rescheduled to date must be within 7 days before or after from and to dates";
            throw new HolidayDateException("repayments.rescheduled.to.must.be.within.range", defaultUserMessage, fromDate.toString(),
                    toDate.toString(), repaymentsRescheduledTo.toString());
        }
    }

}
