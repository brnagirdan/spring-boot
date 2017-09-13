/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.boot.actuate.metrics.web.client;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.stats.hist.Histogram;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

/**
 * Intercepts {@link RestTemplate} requests and records metrics about execution time and
 * results.
 *
 * @author Jon Schneider
 * @since 2.0.0
 */
public class MetricsRestTemplateInterceptor implements ClientHttpRequestInterceptor {

	private final MeterRegistry meterRegistry;

	private final RestTemplateExchangeTagsProvider tagProvider;

	private final String metricName;

	private final boolean recordPercentiles;

	/**
	 * Creates a new {@code MetricsRestTemplateInterceptor} that will record metrics using
	 * the given {@code meterRegistry} with tags provided by the given
	 * {@code tagProvider}.
	 * @param meterRegistry the meter registry
	 * @param tagProvider the tag provider
	 * @param metricName the name of the recorded metric
	 * @param recordPercentiles whether percentile histogram buckets should be recorded
	 */
	public MetricsRestTemplateInterceptor(MeterRegistry meterRegistry,
			RestTemplateExchangeTagsProvider tagProvider, String metricName,
			boolean recordPercentiles) {
		this.tagProvider = tagProvider;
		this.meterRegistry = meterRegistry;
		this.metricName = metricName;
		this.recordPercentiles = recordPercentiles;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {
		long startTime = System.nanoTime();
		ClientHttpResponse response = null;
		try {
			response = execution.execute(request, body);
			return response;
		}
		finally {
			getTimeBuilder(request, response).register(this.meterRegistry)
					.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
			RestTemplateUrlTemplateHolder.clear();
		}
	}

	private Timer.Builder getTimeBuilder(HttpRequest request,
			ClientHttpResponse response) {
		Timer.Builder builder = Timer.builder(this.metricName)
				.tags(this.tagProvider.getTags(request, response))
				.description("Timer of RestTemplate operation");
		if (this.recordPercentiles) {
			builder = builder.histogram(Histogram.percentiles());
		}
		return builder;
	}

}
