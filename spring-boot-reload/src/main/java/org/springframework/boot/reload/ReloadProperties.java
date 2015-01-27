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

package org.springframework.boot.reload;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.springframework.boot.xreload.DefaultReloader;

/**
 * Properties loaded from {@code META-INF/reload.properties} and used to configure
 * {@link DefaultReloader}.
 *
 * @author Phillip Webb
 */
public class ReloadProperties {

	private final Map<?, ?> properties;

	public ReloadProperties() {
		this("META-INF/reload.properties");
	}

	protected ReloadProperties(String source) {
		try {
			Properties properties = new Properties();
			InputStream stream = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(source);
			if (stream != null) {
				try {
					properties.load(stream);
				}
				finally {
					stream.close();
				}
			}
			this.properties = Collections.unmodifiableMap(properties);
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public boolean isDebug() {
		return getProperty("debug", Boolean.class, false);
	}

	public boolean isShowBanner() {
		return getProperty("show-banner", Boolean.class, true);
	}

	public boolean isLiveReload() {
		return getProperty("livereload", Boolean.class, true);
	}

	public boolean isKeepAlive() {
		return getProperty("keep-alive", Boolean.class, true);
	}

	private <T> T getProperty(String name, Class<T> type, T defaultValue) {
		Object value = this.properties.get(name);
		return (value == null ? defaultValue : coerce(value, type));
	}

	@SuppressWarnings("unchecked")
	private <T> T coerce(Object value, Class<T> type) {
		if (type.equals(String.class)) {
			return (T) value.toString();
		}
		if (type.equals(Boolean.class)) {
			return (T) Boolean.valueOf(value.toString());
		}
		throw new IllegalStateException("Unsupported type " + type);
	}

}
