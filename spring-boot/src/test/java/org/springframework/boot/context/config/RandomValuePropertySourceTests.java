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

package org.springframework.boot.context.config;

import java.util.Random;

import org.junit.Test;
import org.mockito.Mockito;





/**
 * Tests for {@link RandomValuePropertySource}.
 *
 * @author Dave Syer
 * @author Matt Benson
 */
public class RandomValuePropertySourceTests {

	private RandomValuePropertySource source = new RandomValuePropertySource();

	@Test
	public void notRandom() {
		assertThat(this.source.getProperty("foo")).isNull();
	}

	@Test
	public void string() {
		assertThat(this.source.getProperty("random.string")).isNotNull();
	}

	@Test
	public void intValue() {
		Integer value = (Integer) this.source.getProperty("random.int");
		assertThat(value).isNotNull();
	}

	@Test
	public void intRange() {
		Integer value = (Integer) this.source.getProperty("random.int[4,10]");
		assertThat(value).isNotNull();
		assertThat(value >= 4).isTrue();
		assertThat(value < 10).isTrue();
	}

	@Test
	public void intMax() {
		Integer value = (Integer) this.source.getProperty("random.int(10)");
		assertThat(value).isNotNull();
		assertThat(value < 10).isTrue();
	}

	@Test
	public void longValue() {
		Long value = (Long) this.source.getProperty("random.long");
		assertThat(value).isNotNull();
	}

	@Test
	public void longRange() {
		Long value = (Long) this.source.getProperty("random.long[4,10]");
		assertThat(value).isNotNull();
		assertThat(Long.toString(value), value >= 4L).isTrue();
		assertThat(Long.toString(value), value < 10L).isTrue();
	}

	@Test
	public void longMax() {
		Long value = (Long) this.source.getProperty("random.long(10)");
		assertThat(value).isNotNull();
		assertThat(value < 10L).isTrue();
	}

	@Test
	public void longOverflow() {
		RandomValuePropertySource source = Mockito.spy(this.source);
		Mockito.when(source.getSource()).thenReturn(new Random() {

			@Override
			public long nextLong() {
				// constant that used to become -8, now becomes 8
				return Long.MIN_VALUE;
			}

		});
		Long value = (Long) source.getProperty("random.long(10)");
		assertThat(value).isNotNull();
		assertThat(value + " is less than 0", value >= 0L).isTrue();
		assertThat(value + " is more than 10", value < 10L).isTrue();

		value = (Long) source.getProperty("random.long[4,10]");
		assertThat(value).isNotNull();
		assertThat(value + " is less than 4", value >= 4L).isTrue();
		assertThat(value + " is more than 10", value < 10L).isTrue();
	}
}
