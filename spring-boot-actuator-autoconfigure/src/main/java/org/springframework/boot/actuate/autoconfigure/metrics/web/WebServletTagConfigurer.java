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

package org.springframework.boot.actuate.autoconfigure.metrics.web;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.micrometer.core.instrument.Tag;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Supplies default tags to meters monitoring the Web MVC server (servlet) programming
 * model.
 *
 * @author Jon Schneider
 * @since 2.0.0
 */
public class WebServletTagConfigurer {

	/**
	 * Supplies default tags to long task timers.
	 * @param request The HTTP request.
	 * @param handler The request method that is responsible for handling the request.
	 * @return A set of tags added to every Spring MVC HTTP request
	 */
	@SuppressWarnings("unused") // we aren't doing anything with the handler by default
	public Iterable<Tag> httpLongRequestTags(HttpServletRequest request, Object handler) {
		return Arrays.asList(method(request), uri(request));
	}

	/**
	 * Supplies default tags to the Web MVC server programming model.
	 * @param request The HTTP request.
	 * @param response The HTTP response.
	 * @param ex The current exception, if any
	 * @return A set of tags added to every Spring MVC HTTP request.
	 */
	public Iterable<Tag> httpRequestTags(HttpServletRequest request,
			HttpServletResponse response, Throwable ex) {
		return Arrays.asList(method(request), uri(request), exception(ex),
				status(response));
	}

	/**
	 * Creates a {@code method} that based on the method of the given {@code request}.
	 * @param request The HTTP request.
	 * @return A "method" tag whose value is a capitalized method (e.g. GET).
	 */
	public Tag method(HttpServletRequest request) {
		return Tag.of("method", request.getMethod());
	}

	/**
	 * Creates a {@code method} that based on the status of the given {@code response}.
	 * @param response The HTTP response.
	 * @return A "status" tag whose value is the numeric status code.
	 */
	public Tag status(HttpServletResponse response) {
		return Tag.of("status", ((Integer) response.getStatus()).toString());
	}

	public Tag uri(HttpServletRequest request) {
		String uri = (String) request
				.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		if (uri == null) {
			uri = request.getPathInfo();
		}
		if (!StringUtils.hasText(uri)) {
			uri = "/";
		}
		return Tag.of("uri", uri.isEmpty() ? "root" : uri);
	}

	public Tag exception(Throwable exception) {
		if (exception != null) {
			return Tag.of("exception", exception.getClass().getSimpleName());
		}
		return Tag.of("exception", "None");
	}

}
