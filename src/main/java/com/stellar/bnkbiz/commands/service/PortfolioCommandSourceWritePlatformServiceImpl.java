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
package com.stellar.bnkbiz.commands.service;

import java.util.Random;

import com.stellar.bnkbiz.commands.domain.CommandSource;
import com.stellar.bnkbiz.commands.domain.CommandSourceRepository;
import com.stellar.bnkbiz.commands.domain.CommandWrapper;
import com.stellar.bnkbiz.commands.exception.CommandNotAwaitingApprovalException;
import com.stellar.bnkbiz.commands.exception.CommandNotFoundException;
import com.stellar.bnkbiz.commands.exception.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.serialization.FromJsonHelper;
import com.stellar.bnkbiz.infrastructure.core.service.ThreadLocalContextUtil;
import com.stellar.bnkbiz.infrastructure.jobs.service.SchedulerJobRunnerReadService;
import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
import com.stellar.bnkbiz.useradministration.domain.AppUser;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;

@Service
public class PortfolioCommandSourceWritePlatformServiceImpl implements PortfolioCommandSourceWritePlatformService {

    private final PlatformSecurityContext context;
    private final CommandSourceRepository commandSourceRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final CommandProcessingService processAndLogCommandService;
    private final SchedulerJobRunnerReadService schedulerJobRunnerReadService;
    private final static Logger logger = LoggerFactory.getLogger(PortfolioCommandSourceWritePlatformServiceImpl.class);

    @Autowired
    public PortfolioCommandSourceWritePlatformServiceImpl(final PlatformSecurityContext context,
            final CommandSourceRepository commandSourceRepository, final FromJsonHelper fromApiJsonHelper,
            final CommandProcessingService processAndLogCommandService, final SchedulerJobRunnerReadService schedulerJobRunnerReadService) {
        this.context = context;
        this.commandSourceRepository = commandSourceRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.processAndLogCommandService = processAndLogCommandService;
        this.schedulerJobRunnerReadService = schedulerJobRunnerReadService;
    }

    @Override
    public CommandProcessingResult logCommandSource(final CommandWrapper wrapper) {

        boolean isApprovedByChecker = false;
        // check if is update of own account details
        if (wrapper.isUpdateOfOwnUserDetails(this.context.authenticatedUser(wrapper).getId())) {
            // then allow this operation to proceed.
            // maker checker doesnt mean anything here.
            isApprovedByChecker = true; // set to true in case permissions have
                                        // been maker-checker enabled by
                                        // accident.
        } else {
            // if not user changing their own details - check user has
            // permission to perform specific task.
            this.context.authenticatedUser(wrapper).validateHasPermissionTo(wrapper.getTaskPermissionName());
        }
        validateIsUpdateAllowed();

        final String json = wrapper.getJson();
        CommandProcessingResult result = null;
        JsonCommand command = null;
        Integer numberOfRetries = 0;
        Integer maxNumberOfRetries = ThreadLocalContextUtil.getTenant().getConnection().getMaxRetriesOnDeadlock();
        Integer maxIntervalBetweenRetries = ThreadLocalContextUtil.getTenant().getConnection().getMaxIntervalBetweenRetries();
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(json);
        command = JsonCommand.from(json, parsedCommand, this.fromApiJsonHelper, wrapper.getEntityName(), wrapper.getEntityId(),
                wrapper.getSubentityId(), wrapper.getGroupId(), wrapper.getClientId(), wrapper.getLoanId(), wrapper.getSavingsId(),
                wrapper.getTransactionId(), wrapper.getHref(), wrapper.getProductId());
        while (numberOfRetries <= maxNumberOfRetries) {
            try {
                result = this.processAndLogCommandService.processAndLogCommand(wrapper, command, isApprovedByChecker);
                numberOfRetries = maxNumberOfRetries + 1;
            } catch (CannotAcquireLockException | ObjectOptimisticLockingFailureException exception) {
                logger.info("The following command " + command.json() + " has been retried  " + numberOfRetries + " time(s)");
                /***
                 * Fail if the transaction has been retired for
                 * maxNumberOfRetries
                 **/
                if (numberOfRetries >= maxNumberOfRetries) {
                    logger.warn("The following command " + command.json() + " has been retried for the max allowed attempts of "
                            + numberOfRetries + " and will be rolled back");
                    throw (exception);
                }
                /***
                 * Else sleep for a random time (between 1 to 10 seconds) and
                 * continue
                 **/
                try {
                    Random random = new Random();
                    int randomNum = random.nextInt(maxIntervalBetweenRetries + 1);
                    Thread.sleep(1000 + (randomNum * 1000));
                    numberOfRetries = numberOfRetries + 1;
                } catch (InterruptedException e) {
                    throw (exception);
                }
            } catch (final RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                numberOfRetries = maxNumberOfRetries + 1;
                result = this.processAndLogCommandService.logCommand(e.getCommandSourceResult());
            }
        }

        return result;
    }

    @Override
    public CommandProcessingResult approveEntry(final Long makerCheckerId) {

        final CommandSource commandSourceInput = validateMakerCheckerTransaction(makerCheckerId);
        validateIsUpdateAllowed();

        final CommandWrapper wrapper = CommandWrapper.fromExistingCommand(makerCheckerId, commandSourceInput.getActionName(),
                commandSourceInput.getEntityName(), commandSourceInput.resourceId(), commandSourceInput.subresourceId(),
                commandSourceInput.getResourceGetUrl(), commandSourceInput.getProductId(), commandSourceInput.getOfficeId(),
                commandSourceInput.getGroupId(), commandSourceInput.getClientId(), commandSourceInput.getLoanId(),
                commandSourceInput.getSavingsId(), commandSourceInput.getTransactionId());
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(commandSourceInput.json());
        final JsonCommand command = JsonCommand.fromExistingCommand(makerCheckerId, commandSourceInput.json(), parsedCommand,
                this.fromApiJsonHelper, commandSourceInput.getEntityName(), commandSourceInput.resourceId(),
                commandSourceInput.subresourceId(), commandSourceInput.getGroupId(), commandSourceInput.getClientId(),
                commandSourceInput.getLoanId(), commandSourceInput.getSavingsId(), commandSourceInput.getTransactionId(),
                commandSourceInput.getResourceGetUrl(), commandSourceInput.getProductId());

        final boolean makerCheckerApproval = true;
        return this.processAndLogCommandService.processAndLogCommand(wrapper, command, makerCheckerApproval);
    }

    @Transactional
    @Override
    public Long deleteEntry(final Long makerCheckerId) {

        validateMakerCheckerTransaction(makerCheckerId);
        validateIsUpdateAllowed();

        this.commandSourceRepository.delete(makerCheckerId);

        return makerCheckerId;
    }

    private CommandSource validateMakerCheckerTransaction(final Long makerCheckerId) {

        final CommandSource commandSourceInput = this.commandSourceRepository.findOne(makerCheckerId);
        if (commandSourceInput == null) { throw new CommandNotFoundException(makerCheckerId); }
        if (!(commandSourceInput.isMarkedAsAwaitingApproval())) { throw new CommandNotAwaitingApprovalException(makerCheckerId); }

        this.context.authenticatedUser().validateHasCheckerPermissionTo(commandSourceInput.getPermissionCode());

        return commandSourceInput;
    }

    private boolean validateIsUpdateAllowed() {
        return this.schedulerJobRunnerReadService.isUpdatesAllowed();

    }

    @Override
    public Long rejectEntry(final Long makerCheckerId) {
        final CommandSource commandSourceInput = validateMakerCheckerTransaction(makerCheckerId);
        validateIsUpdateAllowed();
        final AppUser maker = this.context.authenticatedUser();
        commandSourceInput.markAsRejected(maker, DateTime.now());
        this.commandSourceRepository.save(commandSourceInput);
        return makerCheckerId;
    }
}