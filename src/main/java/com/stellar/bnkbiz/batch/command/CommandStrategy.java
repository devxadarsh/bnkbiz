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
package com.stellar.bnkbiz.batch.command;

import javax.ws.rs.core.UriInfo;

import com.stellar.bnkbiz.batch.domain.BatchRequest;
import com.stellar.bnkbiz.batch.domain.BatchResponse;

/**
 * An interface for various Command Strategies. It contains a single function
 * which returns appropriate response from a particular command strategy.
 * 
 * @author Rishabh Shukla
 * 
 * @see com.stellar.bnkbiz.batch.command.internal.UnknownCommandStrategy
 */
public interface CommandStrategy {

    /**
     * Returns an object of type
     * {@link com.stellar.bnkbiz.batch.domain.BatchResponse}. This takes
     * {@link com.stellar.bnkbiz.batch.domain.BatchRequest} as it's single
     * argument and provides appropriate response.
     * 
     * @param batchRequest
     * @param uriInfo
     * @return BatchResponse
     */
    public BatchResponse execute(BatchRequest batchRequest, UriInfo uriInfo);
}
