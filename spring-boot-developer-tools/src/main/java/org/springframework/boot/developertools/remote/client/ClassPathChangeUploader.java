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

package org.springframework.boot.developertools.remote.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.developertools.classpath.ClassPathChangedEvent;
import org.springframework.boot.developertools.filewatch.ChangedFile;
import org.springframework.boot.developertools.filewatch.ChangedFiles;
import org.springframework.boot.developertools.restart.classloader.ClassLoaderFile;
import org.springframework.boot.developertools.restart.classloader.ClassLoaderFile.Kind;
import org.springframework.boot.developertools.restart.classloader.ClassLoaderFiles;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

/**
 * Listens and pushes any classpath updates to a remote endpoint.
 *
 * @author Phillip Webb
 */
public class ClassPathChangeUploader implements
		ApplicationListener<ClassPathChangedEvent> {

	private static final Map<ChangedFile.Type, ClassLoaderFile.Kind> TYPE_MAPPINGS;
	static {
		Map<ChangedFile.Type, ClassLoaderFile.Kind> map = new HashMap<ChangedFile.Type, ClassLoaderFile.Kind>();
		map.put(ChangedFile.Type.ADD, ClassLoaderFile.Kind.ADDED);
		map.put(ChangedFile.Type.DELETE, ClassLoaderFile.Kind.DELETED);
		map.put(ChangedFile.Type.MODIFY, ClassLoaderFile.Kind.MODIFIED);
		TYPE_MAPPINGS = Collections.unmodifiableMap(map);
	}

	private final URI uri;

	private final ClientHttpRequestFactory requestFactory;

	public ClassPathChangeUploader(String url, ClientHttpRequestFactory requestFactory) {
		Assert.hasLength(url, "URL must not be empty");
		Assert.notNull(requestFactory, "RequestFactory must not be null");
		try {
			this.uri = new URL(url).toURI();
		}
		catch (URISyntaxException ex) {
			throw new IllegalArgumentException("Malformed URL '" + url + "'");
		}
		catch (MalformedURLException ex) {
			throw new IllegalArgumentException("Malformed URL '" + url + "'");
		}
		this.requestFactory = requestFactory;
	}

	@Override
	public void onApplicationEvent(ClassPathChangedEvent event) {
		try {
			ClassLoaderFiles classLoaderFiles = getClassLoaderFiles(event);
			ClientHttpRequest request = this.requestFactory.createRequest(this.uri,
					HttpMethod.POST);
			byte[] bytes = serialize(classLoaderFiles);
			request.getHeaders().setContentLength(bytes.length);
			FileCopyUtils.copy(bytes, request.getBody());
			ClientHttpResponse response = request.execute();
			Assert.state(response.getStatusCode() == HttpStatus.OK, "Unexpected "
					+ response.getStatusCode() + " respponse uploading class files");
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private byte[] serialize(ClassLoaderFiles classLoaderFiles) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(classLoaderFiles);
		objectOutputStream.close();
		return outputStream.toByteArray();
	}

	private ClassLoaderFiles getClassLoaderFiles(ClassPathChangedEvent event)
			throws IOException {
		ClassLoaderFiles classLoaderFiles = new ClassLoaderFiles();
		for (ChangedFiles changedFiles : event.getChangeSet()) {
			String sourceFolder = changedFiles.getSourceFolder().getAbsolutePath();
			for (ChangedFile changedFile : changedFiles) {
				classLoaderFiles.addFile(sourceFolder, changedFile.getRelativeName(),
						asClassLoaderFile(changedFile));
			}
		}
		return classLoaderFiles;
	}

	private ClassLoaderFile asClassLoaderFile(ChangedFile changedFile) throws IOException {
		ClassLoaderFile.Kind kind = TYPE_MAPPINGS.get(changedFile.getType());
		byte[] bytes = (kind == Kind.DELETED ? null : FileCopyUtils
				.copyToByteArray(changedFile.getFile()));
		return new ClassLoaderFile(kind, bytes);
	}

}
