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
package com.stellar.bnkbiz.infrastructure.core.exception;

public class UnrecognizedQueryParamException extends RuntimeException {

    private final String queryParamKey;
    private final String queryParamValue;
    private final Object[] supportedParams;

    public UnrecognizedQueryParamException(final String queryParamKey, final String queryParamValue, final Object... supportedParams) {
        this.queryParamKey = queryParamKey;
        this.queryParamValue = queryParamValue;
        this.supportedParams = supportedParams;
    }

    public String getQueryParamKey() {
        return this.queryParamKey;
    }

    public String getQueryParamValue() {
        return this.queryParamValue;
    }

    public Object[] getSupportedParams() {
        return this.supportedParams;
    }
}