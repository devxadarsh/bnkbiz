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
package com.stellar.bnkbiz.infrastructure.hooks.processor;

import static com.stellar.bnkbiz.infrastructure.hooks.api.HookApiConstants.smsTemplateName;
import static com.stellar.bnkbiz.infrastructure.hooks.api.HookApiConstants.webTemplateName;

import com.stellar.bnkbiz.infrastructure.hooks.domain.Hook;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class HookProcessorProvider implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(
			final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public HookProcessor getProcessor(final Hook hook) {
		HookProcessor processor;
		final String templateName = hook.getHookTemplate().getName();
		if (templateName.equalsIgnoreCase(smsTemplateName)) {
			processor = this.applicationContext.getBean("twilioHookProcessor",
					TwilioHookProcessor.class);
		} else if (templateName.equals(webTemplateName)) {
			processor = this.applicationContext.getBean("webHookProcessor",
					WebHookProcessor.class);
		} else {
			processor = null;
		}
		return processor;
	}

}
