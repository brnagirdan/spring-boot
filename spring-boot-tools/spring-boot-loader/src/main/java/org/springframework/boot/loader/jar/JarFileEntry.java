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

package org.springframework.boot.loader.jar;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;

/**
 * Extended variant of {@link java.util.jar.JarEntry} returned by {@link JarFile}s.
 *
 * @author Phillip Webb
 */
class JarFileEntry extends java.util.jar.JarEntry {

	private Certificate[] certificates;

	private CodeSigner[] codeSigners;

	private final JarFile jarFile;

	JarFileEntry(JarFile jarFile, String name) {
		super(name);
		this.jarFile = jarFile;
	}

	/**
	 * Return a {@link URL} for this {@link JarEntry}.
	 * @return the URL for the entry
	 * @throws MalformedURLException if the URL is not valid
	 */
	URL getUrl() throws MalformedURLException {
		return new URL(this.jarFile.getUrl(), getName());
	}

	@Override
	public Attributes getAttributes() throws IOException {
		Manifest manifest = this.jarFile.getManifest();
		return (manifest == null ? null : manifest.getAttributes(getName()));
	}

	@Override
	public Certificate[] getCertificates() {
		if (this.jarFile.isSigned() && this.certificates == null) {
			this.jarFile.setupEntryCertificates(this);
		}
		return this.certificates;
	}

	@Override
	public CodeSigner[] getCodeSigners() {
		if (this.jarFile.isSigned() && this.codeSigners == null) {
			this.jarFile.setupEntryCertificates(this);
		}
		return this.codeSigners;
	}

	void setCertificates(java.util.jar.JarEntry entry) {
		this.certificates = entry.getCertificates();
		this.codeSigners = entry.getCodeSigners();
	}

}
