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

package org.springframework.boot.developertools.autoconfigure;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * Tests for {@link RemoteDeveloperToolsAutoConfiguration}.
 *
 * @author Rob Winch
 * @since 1.3.0
 */
public class RemoteDeveloperToolsAutoConfigurationTests {

	private AnnotationConfigWebApplicationContext context;

	private MockHttpServletRequest request;

	private MockHttpServletResponse response;

	private MockFilterChain chain;
	// FIXME
	// @Before
	// public void setup() {
	// this.request = new MockHttpServletRequest();
	// this.response = new MockHttpServletResponse();
	// this.chain = new MockFilterChain();
	// }
	//
	// @After
	// public void close() {
	// if (this.context != null) {
	// this.context.close();
	// }
	// }
	//
	// @Test
	// public void defaultSetup() throws Exception {
	// loadContext("spring.developertools.remote.secret:supersecret");
	// HttpTunnelFilter filter = this.context.getBean(HttpTunnelFilter.class);
	// this.request.setRequestURI(RemoteDeveloperToolsProperties.DEFAULT_CONTEXT_PATH
	// + "/debug");
	// this.request.addHeader(RemoteDeveloperToolsProperties.DEFAULT_SECRET_HEADER_NAME,
	// "supersecret");
	// filter.doFilter(this.request, this.response, this.chain);
	// assertTunnel(true);
	// }
	//
	// @Test
	// public void invalidUrlInRequest() throws Exception {
	// loadContext("spring.developertools.remote.secret:supersecret");
	// HttpTunnelFilter filter = this.context.getBean(HttpTunnelFilter.class);
	// this.request.setRequestURI("/debug");
	// this.request.addHeader(RemoteDeveloperToolsProperties.DEFAULT_SECRET_HEADER_NAME,
	// "supersecret");
	// filter.doFilter(this.request, this.response, this.chain);
	// assertTunnel(false);
	// }
	//
	// @Test
	// public void missingSecretInConfigDisables() throws Exception {
	// loadContext("a:b");
	// String[] namesForType = this.context.getBeanNamesForType(HttpTunnelFilter.class);
	// assertThat(namesForType.length, equalTo(0));
	// }
	//
	// @Test
	// public void missingSecretFromRequest() throws Exception {
	// loadContext("spring.developertools.remote.secret:supersecret");
	// HttpTunnelFilter filter = this.context.getBean(HttpTunnelFilter.class);
	// this.request.setRequestURI(RemoteDeveloperToolsProperties.DEFAULT_CONTEXT_PATH
	// + "/debug");
	// filter.doFilter(this.request, this.response, this.chain);
	// assertTunnel(false);
	// }
	//
	// @Test
	// public void invalidSecretInRequest() throws Exception {
	// loadContext("spring.developertools.remote.secret:supersecret");
	// HttpTunnelFilter filter = this.context.getBean(HttpTunnelFilter.class);
	// this.request.setRequestURI(RemoteDeveloperToolsProperties.DEFAULT_CONTEXT_PATH
	// + "/debug");
	// this.request.addHeader(RemoteDeveloperToolsProperties.DEFAULT_SECRET_HEADER_NAME,
	// "invalid");
	// filter.doFilter(this.request, this.response, this.chain);
	// assertTunnel(false);
	// }
	//
	// @Test
	// public void customHeaderName() throws Exception {
	// loadContext("spring.developertools.remote.secret:supersecret",
	// "spring.developertools.remote.secretHeaderName:customheader");
	// HttpTunnelFilter filter = this.context.getBean(HttpTunnelFilter.class);
	// this.request.setRequestURI(RemoteDeveloperToolsProperties.DEFAULT_CONTEXT_PATH
	// + "/debug");
	// this.request.addHeader("customheader", "supersecret");
	// filter.doFilter(this.request, this.response, this.chain);
	// assertTunnel(true);
	// }
	//
	// private void assertTunnel(boolean value) {
	// assertThat(this.context.getBean(MockHttpTunnelServer.class).invoked,
	// equalTo(value));
	// }
	//
	// private void loadContext(String... properties) {
	// this.context = new AnnotationConfigWebApplicationContext();
	// this.context.setServletContext(new MockServletContext());
	// this.context.register(Config.class, ServerPropertiesAutoConfiguration.class,
	// PropertyPlaceholderAutoConfiguration.class);
	// EnvironmentTestUtils.addEnvironment(this.context, properties);
	// this.context.refresh();
	// }
	//
	// @Import(RemoteDeveloperToolsAutoConfiguration.class)
	// @Configuration
	// static class Config {
	//
	// @Bean
	// public HttpTunnelServer remoteDebugHttpTunnelServer() {
	// return new MockHttpTunnelServer(new SocketTargetServerConnection(
	// new RemoteDebugPortProvider()));
	// }
	//
	// }
	//
	// static class MockHttpTunnelServer extends HttpTunnelServer {
	//
	// private boolean invoked;
	//
	// public MockHttpTunnelServer(TargetServerConnection serverConnection) {
	// super(serverConnection);
	// }
	//
	// @Override
	// public void handle(ServerHttpRequest request, ServerHttpResponse response)
	// throws IOException {
	// this.invoked = true;
	// }
	//
	// }
	// FIXME
}
