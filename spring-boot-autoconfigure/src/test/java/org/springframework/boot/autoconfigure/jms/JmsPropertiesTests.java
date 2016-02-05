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

package org.springframework.boot.autoconfigure.jms;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;




/**
 * Tests for {@link JmsProperties}.
 *
 * @author Stephane Nicoll
 */
public class JmsPropertiesTests {

	@Test
	public void formatConcurrencyNull() {
		JmsProperties properties = new JmsProperties();
		assertThat(properties.getListener().formatConcurrency()).isNull();
	}

	@Test
	public void formatConcurrencyOnlyLowerBound() {
		JmsProperties properties = new JmsProperties();
		properties.getListener().setConcurrency(2);
		assertThat(properties.getListener().formatConcurrency()).isEqualTo("2");
	}

	@Test
	public void formatConcurrencyOnlyHigherBound() {
		JmsProperties properties = new JmsProperties();
		properties.getListener().setMaxConcurrency(5);
		assertThat(properties.getListener().formatConcurrency()).isEqualTo("1-5");
	}

	@Test
	public void formatConcurrencyBothBounds() {
		JmsProperties properties = new JmsProperties();
		properties.getListener().setConcurrency(2);
		properties.getListener().setMaxConcurrency(10);
		assertThat(properties.getListener().formatConcurrency()).isEqualTo("2-10");
	}

}
