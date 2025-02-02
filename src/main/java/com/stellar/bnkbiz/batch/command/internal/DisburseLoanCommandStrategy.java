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
package com.stellar.bnkbiz.batch.command.internal;

import javax.ws.rs.core.UriInfo;

import com.stellar.bnkbiz.batch.command.CommandStrategy;
import com.stellar.bnkbiz.batch.domain.BatchRequest;
import com.stellar.bnkbiz.batch.domain.BatchResponse;
import com.stellar.bnkbiz.batch.exception.ErrorHandler;
import com.stellar.bnkbiz.batch.exception.ErrorInfo;
import com.stellar.bnkbiz.portfolio.loanaccount.api.LoansApiResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implements {@link com.stellar.bnkbiz.batch.command.CommandStrategy} to handle
 * disburse of a loan. It passes the contents of the body from the
 * BatchRequest to
 * {@link com.stellar.bnkbiz.portfolio.client.api.LoansApiResource} and gets
 * back the response. This class will also catch any errors raised by
 * {@link com.stellar.bnkbiz.portfolio.client.api.LoansApiResource} and map
 * those errors to appropriate status codes in BatchResponse.
 * 
 * @author Rishabh Shukla
 * 
 * @see com.stellar.bnkbiz.batch.command.CommandStrategy
 * @see com.stellar.bnkbiz.batch.domain.BatchRequest
 * @see com.stellar.bnkbiz.batch.domain.BatchResponse
 */
@Component
public class DisburseLoanCommandStrategy implements CommandStrategy {

    private final LoansApiResource loansApiResource;

    @Autowired
    public DisburseLoanCommandStrategy(final LoansApiResource loansApiResource) {
        this.loansApiResource = loansApiResource;
    }

    @Override
    public BatchResponse execute(final BatchRequest request, @SuppressWarnings("unused") UriInfo uriInfo) {

        final BatchResponse response = new BatchResponse();
        final String responseBody;

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());
        
        final String[] pathParameters = request.getRelativeUrl().split("/");
        Long loanId = Long.parseLong(pathParameters[1].substring(0, pathParameters[1].indexOf("?")));

        // Try-catch blocks to map exceptions to appropriate status codes
        try {

            // Calls 'disburse' function from 'LoansApiResource' to disburse a loan          
            responseBody = loansApiResource.stateTransitions(loanId, "disburse", request.getBody());

            response.setStatusCode(200);
            
            // Sets the body of the response after the successful disbursal of the loan
            response.setBody(responseBody);

        } catch (RuntimeException e) {

            // Gets an object of type ErrorInfo, containing information about
            // raised exception
            ErrorInfo ex = ErrorHandler.handler(e);

            response.setStatusCode(ex.getStatusCode());
            response.setBody(ex.getMessage());
        }

        return response;
    }

}
