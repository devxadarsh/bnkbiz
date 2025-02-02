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
package com.stellar.bnkbiz.portfolio.floatingrates.domain;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface FloatingRateRepository extends
		JpaRepository<FloatingRate, Long>,
		JpaSpecificationExecutor<FloatingRate> {

	@Query("from FloatingRate floatingRate where floatingRate.isBaseLendingRate = 1 and floatingRate.isActive = 1")
	FloatingRate retrieveBaseLendingRate();
	
	@Query("from FloatingRate floatingRate " +
			" inner join floatingRate.floatingRatePeriods as periods" +
			" where floatingRate.isActive = 1 " +
			" and periods.isActive = 1 " +
			" and periods.isDifferentialToBaseLendingRate = 1")
	Collection<FloatingRate> retrieveFloatingRatesLinkedToBLR();

}
