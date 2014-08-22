/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.jms.hornetq;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.hornetq.jms.server.embedded.EmbeddedJMS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for HornetQ {@link ConnectionFactory}.
 *
 * @author Phillip Webb
 * @since 1.2.0
 */
@Configuration
@ConditionalOnMissingBean(ConnectionFactory.class)
class HornetQConnectionFactoryConfiguration {

	private static Log logger = LogFactory
			.getLog(HornetQEmbeddedServerConfiguration.class);

	// Ensure JMS is setup before XA
	@Autowired(required = false)
	private EmbeddedJMS embeddedJMS;

	@Bean
	public ConnectionFactory jmsConnectionFactory(HornetQProperties properties) {
		if (this.embeddedJMS != null && logger.isDebugEnabled()) {
			logger.debug("Using embdedded HornetQ borker");
		}
		return new HornetQConnectionFactoryFactory(properties)
				.createConnectionFactory(HornetQConnectionFactory.class);
	}

}
