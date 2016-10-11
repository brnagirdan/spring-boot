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

package org.springframework.boot.actuate.cloudfoundry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.NamedMvcEndpoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * {@link HandlerMapping} to map {@link Endpoint}s to Cloud Foundry specific URLs.
 *
 * @author Madhura Bhave
 */
class CloudFoundryEndpointHandlerMapping extends EndpointHandlerMapping {

	CloudFoundryEndpointHandlerMapping(Collection<? extends NamedMvcEndpoint> endpoints) {
		super(endpoints);
	}

	CloudFoundryEndpointHandlerMapping(Set<NamedMvcEndpoint> endpoints,
			CorsConfiguration corsConfiguration) {
		super(endpoints, corsConfiguration);
	}

	@Override
	protected String getPath(MvcEndpoint endpoint) {
		if (endpoint instanceof NamedMvcEndpoint) {
			return "/" + ((NamedMvcEndpoint) endpoint).getName();
		}
		return super.getPath(endpoint);
	}

	@Override
	protected HandlerExecutionChain getHandlerExecutionChain(Object handler,
			HttpServletRequest request) {
		HandlerExecutionChain chain = super.getHandlerExecutionChain(handler, request);
		List<HandlerInterceptor> interceptors = new ArrayList<HandlerInterceptor>();
		interceptors.add(new SecurityInterceptor());
		if (chain.getInterceptors() != null) {
			interceptors.addAll(Arrays.asList(chain.getInterceptors()));
		}
		return new HandlerExecutionChain(chain.getHandler(),
				interceptors.toArray(new HandlerInterceptor[interceptors.size()]));
	}

	/**
	 * The security interceptor that secures cloudfoundry actuator endpoints.
	 *
	 */
	static class SecurityInterceptor extends HandlerInterceptorAdapter {

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
				Object handler) throws Exception {
			// FIXME
			return true;
		}

	}
}
