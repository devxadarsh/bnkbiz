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
package com.stellar.bnkbiz.portfolio.savings.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.stellar.bnkbiz.accounting.common.AccountingDropdownReadPlatformService;
import com.stellar.bnkbiz.accounting.common.AccountingEnumerations;
import com.stellar.bnkbiz.accounting.common.AccountingRuleType;
import com.stellar.bnkbiz.accounting.glaccount.data.GLAccountData;
import com.stellar.bnkbiz.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import com.stellar.bnkbiz.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import com.stellar.bnkbiz.accounting.producttoaccountmapping.service.ProductToGLAccountMappingReadPlatformService;
import com.stellar.bnkbiz.commands.domain.CommandWrapper;
import com.stellar.bnkbiz.commands.service.CommandWrapperBuilder;
import com.stellar.bnkbiz.commands.service.PortfolioCommandSourceWritePlatformService;
import com.stellar.bnkbiz.infrastructure.core.api.ApiRequestParameterHelper;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.data.EnumOptionData;
import com.stellar.bnkbiz.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import com.stellar.bnkbiz.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import com.stellar.bnkbiz.infrastructure.security.service.PlatformSecurityContext;
import com.stellar.bnkbiz.organisation.monetary.data.CurrencyData;
import com.stellar.bnkbiz.organisation.monetary.service.CurrencyReadPlatformService;
import com.stellar.bnkbiz.portfolio.charge.data.ChargeData;
import com.stellar.bnkbiz.portfolio.charge.service.ChargeReadPlatformService;
import com.stellar.bnkbiz.portfolio.paymenttype.data.PaymentTypeData;
import com.stellar.bnkbiz.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import com.stellar.bnkbiz.portfolio.savings.SavingsApiConstants;
import com.stellar.bnkbiz.portfolio.savings.SavingsCompoundingInterestPeriodType;
import com.stellar.bnkbiz.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import com.stellar.bnkbiz.portfolio.savings.SavingsInterestCalculationType;
import com.stellar.bnkbiz.portfolio.savings.SavingsPostingInterestPeriodType;
import com.stellar.bnkbiz.portfolio.savings.data.SavingsProductData;
import com.stellar.bnkbiz.portfolio.savings.service.SavingsDropdownReadPlatformService;
import com.stellar.bnkbiz.portfolio.savings.service.SavingsEnumerations;
import com.stellar.bnkbiz.portfolio.savings.service.SavingsProductReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Path("/savingsproducts")
@Component
@Scope("singleton")
public class SavingsProductsApiResource {

    private final SavingsProductReadPlatformService savingProductReadPlatformService;
    private final SavingsDropdownReadPlatformService dropdownReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<SavingsProductData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;
    private final ProductToGLAccountMappingReadPlatformService accountMappingReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;

    @Autowired
    public SavingsProductsApiResource(final SavingsProductReadPlatformService savingProductReadPlatformService,
            final SavingsDropdownReadPlatformService dropdownReadPlatformService,
            final CurrencyReadPlatformService currencyReadPlatformService, final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<SavingsProductData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService,
            final ProductToGLAccountMappingReadPlatformService accountMappingReadPlatformService,
            final ChargeReadPlatformService chargeReadPlatformService, PaymentTypeReadPlatformService paymentTypeReadPlatformService) {
        this.savingProductReadPlatformService = savingProductReadPlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.accountingDropdownReadPlatformService = accountingDropdownReadPlatformService;
        this.accountMappingReadPlatformService = accountMappingReadPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String create(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createSavingProduct().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String update(@PathParam("productId") final Long productId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateSavingProduct(productId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_PRODUCT_RESOURCE_NAME);

        final Collection<SavingsProductData> products = this.savingProductReadPlatformService.retrieveAll();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, products, SavingsApiConstants.SAVINGS_PRODUCT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@PathParam("productId") final Long productId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_PRODUCT_RESOURCE_NAME);

        SavingsProductData savingProductData = this.savingProductReadPlatformService.retrieveOne(productId);

        final Collection<ChargeData> charges = this.chargeReadPlatformService.retrieveSavingsProductCharges(productId);

        savingProductData = SavingsProductData.withCharges(savingProductData, charges);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (savingProductData.hasAccountingEnabled()) {
            final Map<String, Object> accountingMappings = this.accountMappingReadPlatformService
                    .fetchAccountMappingDetailsForSavingsProduct(productId, savingProductData.accountingRuleTypeId());
            final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings = this.accountMappingReadPlatformService
                    .fetchPaymentTypeToFundSourceMappingsForSavingsProduct(productId);
            Collection<ChargeToGLAccountMapper> feeToGLAccountMappings = this.accountMappingReadPlatformService
                    .fetchFeeToIncomeAccountMappingsForSavingsProduct(productId);
            Collection<ChargeToGLAccountMapper> penaltyToGLAccountMappings = this.accountMappingReadPlatformService
                    .fetchPenaltyToIncomeAccountMappingsForSavingsProduct(productId);
            savingProductData = SavingsProductData.withAccountingDetails(savingProductData, accountingMappings,
                    paymentChannelToFundSourceMappings, feeToGLAccountMappings, penaltyToGLAccountMappings);
        }

        if (settings.isTemplate()) {
            savingProductData = handleTemplateRelatedData(savingProductData);
        }

        return this.toApiJsonSerializer
                .serialize(settings, savingProductData, SavingsApiConstants.SAVINGS_PRODUCT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_PRODUCT_RESOURCE_NAME);

        final SavingsProductData savingProduct = handleTemplateRelatedData(null);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, savingProduct, SavingsApiConstants.SAVINGS_PRODUCT_RESPONSE_DATA_PARAMETERS);
    }

    private SavingsProductData handleTemplateRelatedData(final SavingsProductData savingsProduct) {

        final EnumOptionData interestCompoundingPeriodType = SavingsEnumerations
                .compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.DAILY);

        final EnumOptionData interestPostingPeriodType = SavingsEnumerations
                .interestPostingPeriodType(SavingsPostingInterestPeriodType.MONTHLY);

        final EnumOptionData interestCalculationType = SavingsEnumerations
                .interestCalculationType(SavingsInterestCalculationType.DAILY_BALANCE);

        final EnumOptionData interestCalculationDaysInYearType = SavingsEnumerations
                .interestCalculationDaysInYearType(SavingsInterestCalculationDaysInYearType.DAYS_365);

        final EnumOptionData accountingRule = AccountingEnumerations.accountingRuleType(AccountingRuleType.NONE);

        CurrencyData currency = CurrencyData.blank();
        final Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();
        if (currencyOptions.size() == 1) {
            currency = new ArrayList<>(currencyOptions).get(0);
        }

        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = this.dropdownReadPlatformService
                .retrieveCompoundingInterestPeriodTypeOptions();

        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = this.dropdownReadPlatformService
                .retrieveInterestPostingPeriodTypeOptions();

        final Collection<EnumOptionData> interestCalculationTypeOptions = this.dropdownReadPlatformService
                .retrieveInterestCalculationTypeOptions();

        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = this.dropdownReadPlatformService
                .retrieveInterestCalculationDaysInYearTypeOptions();

        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = this.dropdownReadPlatformService
                .retrieveLockinPeriodFrequencyTypeOptions();

        final Collection<EnumOptionData> withdrawalFeeTypeOptions = this.dropdownReadPlatformService.retrievewithdrawalFeeTypeOptions();

        final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();

        final Collection<EnumOptionData> accountingRuleOptions = this.accountingDropdownReadPlatformService
                .retrieveAccountingRuleTypeOptions();
        final Map<String, List<GLAccountData>> accountingMappingOptions = this.accountingDropdownReadPlatformService
                .retrieveAccountMappingOptionsForSavingsProducts();

        // charges
        final boolean feeChargesOnly = true;
        Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveSavingsProductApplicableCharges(feeChargesOnly);
        chargeOptions = CollectionUtils.isEmpty(chargeOptions) ? null : chargeOptions;

        Collection<ChargeData> penaltyOptions = this.chargeReadPlatformService.retrieveSavingsApplicablePenalties();
        penaltyOptions = CollectionUtils.isEmpty(penaltyOptions) ? null : penaltyOptions;

        SavingsProductData savingsProductToReturn = null;
        if (savingsProduct != null) {
            savingsProductToReturn = SavingsProductData.withTemplate(savingsProduct, currencyOptions, interestCompoundingPeriodTypeOptions,
                    interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                    lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions, accountingRuleOptions,
                    accountingMappingOptions, chargeOptions, penaltyOptions);
        } else {
            savingsProductToReturn = SavingsProductData.template(currency, interestCompoundingPeriodType, interestPostingPeriodType,
                    interestCalculationType, interestCalculationDaysInYearType, accountingRule, currencyOptions,
                    interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                    interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions,
                    paymentTypeOptions, accountingRuleOptions, accountingMappingOptions, chargeOptions, penaltyOptions);
        }

        return savingsProductToReturn;
    }

    @DELETE
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String delete(@PathParam("productId") final Long productId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteSavingProduct(productId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }
}