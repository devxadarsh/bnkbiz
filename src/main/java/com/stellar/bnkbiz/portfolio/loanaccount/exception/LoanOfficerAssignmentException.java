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
import java.time.LocalDate;

public class LoanOfficerAssignmentException extends AbstractPlatformDomainRuleException {

    public LoanOfficerAssignmentException(final Long loanId, final Long fromLoanOfficerId) {
        super("error.msg.loan.not.assigned.to.loan.officer", "Loan with identifier " + loanId
                + " is not assigned to Loan Officer with identifier " + fromLoanOfficerId + ".", loanId);
    }

    public LoanOfficerAssignmentException(final Long loanId, final LocalDate date) {
        super("error.msg.loan.assignment.date.is.before.last.assignment.date", "Loan with identifier " + loanId
                + " was already assigned before date " + date.toString());
    }
}
