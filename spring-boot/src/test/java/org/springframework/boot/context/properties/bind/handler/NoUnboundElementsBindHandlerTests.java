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

package org.springframework.boot.context.properties.bind.handler;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MockConfigurationPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests for {@link NoUnboundElementsBindHandler}.
 *
 * @author Phillip Webb
 */
public class NoUnboundElementsBindHandlerTests {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private List<ConfigurationPropertySource> sources = new ArrayList<>();

	private Binder binder;

	@Test
	public void bindWhenNotUsingNoUnboundElementsHandlerShouldBind() throws Exception {
		MockConfigurationPropertySource source = new MockConfigurationPropertySource();
		source.put("example.foo", "bar");
		source.put("example.baz", "bar");
		this.sources.add(source);
		this.binder = new Binder(this.sources);
		Example bound = this.binder.bind("example", Bindable.of(Example.class));
		assertThat(bound.getFoo()).isEqualTo("bar");
	}

	@Test
	public void bindWhenUsingNoUnboundElementsHandlerShouldBind() throws Exception {
		MockConfigurationPropertySource source = new MockConfigurationPropertySource();
		source.put("example.foo", "bar");
		this.sources.add(source);
		this.binder = new Binder(this.sources);
		Example bound = this.binder.bind("example", Bindable.of(Example.class),
				new NoUnboundElementsBindHandler());
		assertThat(bound.getFoo()).isEqualTo("bar");
	}

	@Test
	public void bindWhenUsingNoUnboundElementsHandlerThrowException() throws Exception {
		MockConfigurationPropertySource source = new MockConfigurationPropertySource();
		source.put("example.foo", "bar");
		source.put("example.baz", "bar");
		this.sources.add(source);
		this.binder = new Binder(this.sources);
		try {
			this.binder.bind("example", Bindable.of(Example.class),
					new NoUnboundElementsBindHandler());
			fail("did not throw");
		}
		catch (BindException ex) {
			assertThat(ex.getCause().getMessage())
					.contains("The elements [example.baz] were left unbound");
		}
	}

	@Test
	public void bindWhenUsingNoUnboundElementsHandlerShouldBindIfPrefixDifferent()
			throws Exception {
		MockConfigurationPropertySource source = new MockConfigurationPropertySource();
		source.put("example.foo", "bar");
		source.put("other.baz", "bar");
		this.sources.add(source);
		this.binder = new Binder(this.sources);
		Example bound = this.binder.bind("example", Bindable.of(Example.class),
				new NoUnboundElementsBindHandler());
		assertThat(bound.getFoo()).isEqualTo("bar");
	}

	// FIXME a lot more tests are needed

	public static class Example {

		private String foo;

		public String getFoo() {
			return this.foo;
		}

		public void setFoo(String foo) {
			this.foo = foo;
		}

	}

}
