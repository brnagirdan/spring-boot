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

package org.springframework.boot.autoconfigure.jms;

import java.io.File;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory;
import org.hornetq.core.server.JournalType;
import org.springframework.boot.autoconfigure.jms.HornetQProperties.Embedded;

/**
 * Factory class to create a Hornet {@link Configuration} from {@link HornetQProperties}.
 * 
 * @author Stephane Nicol
 * @author Phillip Webb
 * @since 1.1.0
 */
class HornetQEmbeddedConfigurationFactory {

	private Embedded properties;

	public HornetQEmbeddedConfigurationFactory(HornetQProperties properties) {
		this.properties = properties.getEmbedded();
	}

	public Configuration createConfiguration() {
		ConfigurationImpl configuration = new ConfigurationImpl();
		configuration.setSecurityEnabled(false);
		configuration.setPersistenceEnabled(this.properties.isPersistent());

		String dataDir = getDataDir();

		// HORNETQ-1302
		configuration.setJournalDirectory(dataDir + "/journal");

		if (this.properties.isPersistent()) {
			configuration.setJournalType(JournalType.NIO);
			configuration.setLargeMessagesDirectory(dataDir + "/largemessages");
			configuration.setBindingsDirectory(dataDir + "/bindings");
			configuration.setPagingDirectory(dataDir + "/paging");
		}

		TransportConfiguration transportConfiguration = new TransportConfiguration(
				InVMAcceptorFactory.class.getName());
		configuration.getAcceptorConfigurations().add(transportConfiguration);

		// HORNETQ-1143
		configuration.setClusterPassword("SpringBootRules");
		return configuration;
	}

	private String getDataDir() {
		if (this.properties.getDataDirectory() != null) {
			return this.properties.getDataDirectory();
		}
		String tempDirectory = System.getProperty("java.io.tmpdir");
		return new File(tempDirectory, "hornetq-data").getAbsolutePath();
	}
}
