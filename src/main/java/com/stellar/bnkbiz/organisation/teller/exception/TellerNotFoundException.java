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
package com.stellar.bnkbiz.organisation.teller.exception;

import com.stellar.bnkbiz.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * Indicates that a teller could not be found.
 *
 * @author Markus Geiss
 * @since 2.0.0
 */
public class TellerNotFoundException extends AbstractPlatformResourceNotFoundException {

    private static final String ERROR_MESSAGE_CODE = "error.msg.teller.not.found";
    private static final String DEFAULT_ERROR_MESSAGE = "Teller with identifier {0,number,long} not found!";

    /**
     * Creates a new instance.
     *
     * @param tellerId the primary key of the teller
     */
    public TellerNotFoundException(Long tellerId) {
        super(ERROR_MESSAGE_CODE, DEFAULT_ERROR_MESSAGE, tellerId);
    }
}
