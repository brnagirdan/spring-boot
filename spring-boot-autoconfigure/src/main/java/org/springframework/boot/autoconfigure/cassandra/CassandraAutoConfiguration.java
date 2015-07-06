/*
 * Copyright 2012-2015 the original author or authors.
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

package org.springframework.boot.autoconfigure.cassandra;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ProtocolOptions.Compression;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.ReconnectionPolicy;
import com.datastax.driver.core.policies.RetryPolicy;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration} for Cassandra.
 *
 * @author Julien Dubois
 * @since 1.3.0
 */
@Configuration
@ConditionalOnClass({ Cluster.class })
@EnableConfigurationProperties(CassandraProperties.class)
public class CassandraAutoConfiguration {

	private static final Log logger = LogFactory.getLog(CassandraAutoConfiguration.class);

	@Autowired
	private CassandraProperties properties;

	@Bean
	@ConditionalOnMissingBean
	public Cluster cluster() {
		Cluster.Builder builder = Cluster.builder()
				.withClusterName(this.properties.getClusterName())
				.withPort(this.properties.getPort());
		builder.withCompression(getCompression(this.properties.getCompression()));
		if (StringUtils.hasLength(this.properties.getLoadBalancingPolicy())) {
			builder.withLoadBalancingPolicy(getLoadBalancingPolicy(this.properties
					.getLoadBalancingPolicy()));
		}
		builder.withQueryOptions(getQueryOptions());
		if (StringUtils.hasLength(this.properties.getReconnectionPolicy())) {
			builder.withReconnectionPolicy(getReconnectionPolicy(this.properties
					.getReconnectionPolicy()));
		}

		// Manage the retry policy
		if (!StringUtils.isEmpty(this.properties.getRetryPolicy())) {
			try {
				Class retryPolicyClass = ClassUtils.forName(
						this.properties.getRetryPolicy(), null);
				Object retryPolicyInstance = retryPolicyClass.newInstance();
				RetryPolicy userRetryPolicy = (RetryPolicy) retryPolicyInstance;
				builder.withRetryPolicy(userRetryPolicy);
			}
			catch (ClassNotFoundException e) {
				logger.warn(
						"The retry policy could not be loaded, falling back to the default policy",
						e);
			}
			catch (InstantiationException e) {
				logger.warn(
						"The retry policy could not be instanced, falling back to the default policy",
						e);
			}
			catch (IllegalAccessException e) {
				logger.warn(
						"The retry policy could not be created, falling back to the default policy",
						e);
			}
			catch (ClassCastException e) {
				logger.warn(
						"The retry policy does not implement the correct interface, falling back to the default policy",
						e);
			}
		}

		// Manage socket options
		SocketOptions socketOptions = new SocketOptions();
		socketOptions.setConnectTimeoutMillis(this.properties.getConnectTimeoutMillis());
		socketOptions.setReadTimeoutMillis(this.properties.getReadTimeoutMillis());
		builder.withSocketOptions(socketOptions);

		// Manage SSL
		if (this.properties.isSsl()) {
			builder.withSSL();
		}

		// Manage the contact points
		builder.addContactPoints(StringUtils
				.commaDelimitedListToStringArray(this.properties.getContactPoints()));

		return builder.build();
	}

	private Compression getCompression(CassandraProperties.Compression compression) {
		if (compression == null) {
			return Compression.NONE;
		}
		return Compression.valueOf(compression.name());
	}

	private LoadBalancingPolicy getLoadBalancingPolicy(String loadBalancingPolicy) {
		Class<?> policyClass = ClassUtils.resolveClassName(loadBalancingPolicy, null);
		Assert.isAssignable(LoadBalancingPolicy.class, policyClass);
		return (LoadBalancingPolicy) BeanUtils.instantiate(policyClass);
	}

	private QueryOptions getQueryOptions() {
		QueryOptions options = new QueryOptions();
		if (this.properties.getConsistencyLevel() != null) {
			options.setConsistencyLevel(ConsistencyLevel.valueOf(this.properties
					.getConsistencyLevel().name()));
		}
		if (this.properties.getSerialConsistencyLevel() != null) {
			options.setSerialConsistencyLevel(ConsistencyLevel.valueOf(this.properties
					.getSerialConsistencyLevel().name()));
		}
		options.setFetchSize(this.properties.getFetchSize());
		return options;
	}

	private ReconnectionPolicy getReconnectionPolicy(String reconnectionPolicy) {
		Class<?> policyClass = ClassUtils.resolveClassName(reconnectionPolicy, null);
		Assert.isAssignable(ReconnectionPolicy.class, policyClass);
		return (ReconnectionPolicy) BeanUtils.instantiate(policyClass);
	}

}
