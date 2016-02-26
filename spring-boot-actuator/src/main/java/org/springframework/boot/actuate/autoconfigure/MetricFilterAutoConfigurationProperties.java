/*
 * Copyright 2012-2016 the original author or authors.
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

package org.springframework.boot.actuate.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the {@link MetricsFilter}.
 *
 * @author Sebastian Kirsch
 * @since 1.4.0
 */
@ConfigurationProperties("endpoints.metrics.filter")
public class MetricFilterAutoConfigurationProperties {

	/**
	 * Record the request count and measure processing times based on the request URI.
	 */
	private boolean recordRolledUpMetrics = true;

	/**
	 * Additionally record the metrics based on the request URI and HTTP method.
	 */
	private boolean recordMetricsPerHttpMethod = false;

	public boolean isRecordMetricsPerHttpMethod() {
		return this.recordMetricsPerHttpMethod;
	}

	public void setRecordMetricsPerHttpMethod(boolean recordMetricsPerHttpMethod) {
		this.recordMetricsPerHttpMethod = recordMetricsPerHttpMethod;
	}

	public boolean isRecordRolledUpMetrics() {
		return this.recordRolledUpMetrics;
	}

	public void setRecordRolledUpMetrics(boolean recordRolledUpMetrics) {
		this.recordRolledUpMetrics = recordRolledUpMetrics;
	}

}
