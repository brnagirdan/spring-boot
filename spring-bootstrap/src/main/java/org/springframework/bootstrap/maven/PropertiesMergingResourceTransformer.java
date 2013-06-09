/*
 * Copyright 2012-2013 the original author or authors.
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

package org.springframework.bootstrap.maven;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.maven.plugins.shade.relocation.Relocator;
import org.apache.maven.plugins.shade.resource.ResourceTransformer;

/**
 * Extension for the <a href="http://maven.apache.org/plugins/maven-shade-plugin/">Maven
 * shade plugin</a> to allow properties files (e.g. <code>META-INF/spring.factories</code>
 * ) to be merged without losing any information.
 * 
 * @author Dave Syer
 */
public class PropertiesMergingResourceTransformer implements ResourceTransformer {

	// FIXME move out of core

	String resource; // Set this in pom configuration with <resource>...</resource>

	private Properties data = new Properties();

	/**
	 * @return the data the properties being merged
	 */
	public Properties getData() {
		return this.data;
	}

	@Override
	public boolean canTransformResource(String resource) {
		if (this.resource != null && this.resource.equalsIgnoreCase(resource)) {
			return true;
		}
		return false;
	}

	@Override
	public void processResource(String resource, InputStream is,
			List<Relocator> relocators) throws IOException {
		Properties properties = new Properties();
		properties.load(is);
		is.close();
		for (Object key : properties.keySet()) {
			String name = (String) key;
			String value = properties.getProperty(name);
			String existing = this.data.getProperty(name);
			this.data
					.setProperty(name, existing == null ? value : existing + "," + value);
		}
	}

	@Override
	public boolean hasTransformedResource() {
		return this.data.size() > 0;
	}

	@Override
	public void modifyOutputStream(JarOutputStream os) throws IOException {
		os.putNextEntry(new JarEntry(this.resource));
		this.data.store(os, "Merged by PropertiesMergingResourceTransformer");
		os.flush();
		this.data.clear();
	}

}
