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
package com.stellar.bnkbiz.portfolio.shares.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import com.stellar.bnkbiz.infrastructure.core.api.JsonCommand;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResult;
import com.stellar.bnkbiz.infrastructure.core.data.CommandProcessingResultBuilder;
import com.stellar.bnkbiz.infrastructure.core.serialization.FromJsonHelper;
import com.stellar.bnkbiz.organisation.monetary.data.CurrencyData;
import com.stellar.bnkbiz.organisation.monetary.domain.MonetaryCurrency;
import com.stellar.bnkbiz.portfolio.accounts.domain.ShareAccount;
import com.stellar.bnkbiz.portfolio.products.service.ProductCommandsService;
import com.stellar.bnkbiz.portfolio.shares.constants.ShareProductApiConstants;
import com.stellar.bnkbiz.portfolio.shares.data.DividendsData;
import com.stellar.bnkbiz.portfolio.shares.data.ProductDividendsData;
import com.stellar.bnkbiz.portfolio.shares.domain.ShareProduct;
import com.stellar.bnkbiz.portfolio.shares.domain.ShareProductTempRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service(value = "SHAREPRODUCT_COMMANDSERVICE")
public class ShareProductCommandsServiceImpl implements ProductCommandsService {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ShareProductCommandsServiceImpl(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public ProductDividendsData previewDividends(Long productId, JsonCommand jsonCommand) {
        ArrayList<ShareAccount> accounts = ShareProductTempRepository.getInstance().getAllAccounts(productId);
        ShareProduct product = ShareProductTempRepository.getInstance().fineOne(productId);
        Long total = product.getTotalShares();
        JsonElement element = jsonCommand.parsedJson();
        final BigDecimal totalDividendAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("dividendAmount", element);
        BigDecimal perShareValue = totalDividendAmount.divide(new BigDecimal(total));
        Date date = new Date();
        ArrayList<DividendsData> dividends = new ArrayList<>();
        for (ShareAccount account : accounts) {
            if(account.getStatus().equals("Approved")) {
                BigDecimal val = perShareValue.multiply(new BigDecimal(account.getTotalShares()));
                DividendsData data = new DividendsData(account.getClientId(), account.getClientName(), account.getSavingsAccountNo(),
                        account.getTotalShares(), val, date);
                dividends.add(data);    
            }
        }
        
        MonetaryCurrency currency = product.getCurrency() ;
        CurrencyData cur =  new CurrencyData(currency.getCode(), "", currency.getDigitsAfterDecimal(), currency.getCurrencyInMultiplesOf(),
                "", "");
        ProductDividendsData toReturn = new ProductDividendsData(productId, product.getProductName(), date, totalDividendAmount, cur, dividends);
        return toReturn;
    }

    public CommandProcessingResult postDividends(Long productId, JsonCommand jsonCommand) {
        try {
            ProductDividendsData data = previewDividends(productId, jsonCommand);
            ShareProductTempRepository.getInstance().saveDividends(data);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(data.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            return CommandProcessingResult.empty();
        }
    }

    @Override
    public Object handleCommand(Long productId, String command, String jsonBody) {
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(jsonBody);
        final JsonCommand jsonCommand = JsonCommand.from(jsonBody, parsedCommand, this.fromApiJsonHelper, null, null, null, null, null,
                null, null, null, null, null);
        if (ShareProductApiConstants.PREIEW_DIVIDENDS_COMMAND_STRING.equals(command)) {
            return previewDividends(productId, jsonCommand);
        } else if (ShareProductApiConstants.POST_DIVIDENdS_COMMAND_STRING.equals(command)) { return postDividends(productId,
                jsonCommand); }
        // throw unknow commandexception
        return CommandProcessingResult.empty();
    }

}
