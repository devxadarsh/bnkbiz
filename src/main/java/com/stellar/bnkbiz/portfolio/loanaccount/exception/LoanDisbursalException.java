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
package com.stellar.bnkbiz.portfolio.loanaccount.exception;

import com.stellar.bnkbiz.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class LoanDisbursalException extends AbstractPlatformDomainRuleException {

    public LoanDisbursalException(final String currentProduct, final String restrictedProduct) {
        super("error.msg.loan.disbursal.failed", "This loan could not be disbursed as `" + currentProduct + "` and `" + restrictedProduct
                + "` are not allowed to co-exist", new Object[] { currentProduct, restrictedProduct });
    }

    public LoanDisbursalException(final String defaultUserMessage, final String entity, final Object... defaultUserMessageArgs) {
        super("error.msg.loan." + entity, defaultUserMessage, defaultUserMessageArgs);
    }
}
