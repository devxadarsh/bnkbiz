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
package com.stellar.bnkbiz.infrastructure.core.exceptionmapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.stellar.bnkbiz.infrastructure.core.data.ApiGlobalErrorResponse;
import com.stellar.bnkbiz.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link AbstractPlatformDomainRuleException}
 * thrown by platform into a HTTP API friendly format.
 * 
 * The {@link AbstractPlatformDomainRuleException} is thrown when an api call
 * results is some internal business/domain logic been violated.
 */
@Provider
@Component
@Scope("singleton")
public class PlatformDomainRuleExceptionMapper implements ExceptionMapper<AbstractPlatformDomainRuleException> {

    @Override
    public Response toResponse(final AbstractPlatformDomainRuleException exception) {

        final ApiGlobalErrorResponse notFoundErrorResponse = ApiGlobalErrorResponse.domainRuleViolation(
                exception.getGlobalisationMessageCode(), exception.getDefaultUserMessage(), exception.getDefaultUserMessageArgs());
        // request understood but not carried out due to it violating some
        // domain/business logic
        return Response.status(Status.FORBIDDEN).entity(notFoundErrorResponse).type(MediaType.APPLICATION_JSON).build();
    }
}