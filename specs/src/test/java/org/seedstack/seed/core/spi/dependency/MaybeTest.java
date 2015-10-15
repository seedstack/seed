/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.spi.dependency;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.core.spi.dependency.Maybe;

/**
 * Unit test for {@link Maybe}.
 * @author thierry.bouvet@mpsa.com
 *
 */
public class MaybeTest {

	private static final String CONTENT = "dummy";

	@Test
	public void testHashCode() {
		Maybe<String> maybe = new Maybe<String>(null);
		Assertions.assertThat(maybe.hashCode()).isEqualTo(0);
		maybe = new Maybe<String>(CONTENT);
		Assertions.assertThat(maybe.hashCode()).isNotEqualTo(0);
	}

	@Test
	public void testGet() {
		Maybe<String> maybe = new Maybe<String>(null);
		Assertions.assertThat(maybe.isPresent()).isFalse();
		Assertions.assertThat(maybe.get()).isNull();
		maybe = new Maybe<String>(CONTENT);
		Assertions.assertThat(maybe.isPresent()).isTrue();
		Assertions.assertThat(maybe.get()).isEqualTo(CONTENT);
	}

	@Test
	public void testEqualsObject() {
		Maybe<String> maybe = new Maybe<String>(null);
		Assertions.assertThat(maybe.equals(CONTENT)).isFalse();
		Assertions.assertThat(maybe.equals(null)).isFalse();
		maybe = new Maybe<String>(CONTENT);
		Assertions.assertThat(maybe.equals(maybe)).isTrue();
		Maybe<String> maybe2 = new Maybe<String>(null);
		Assertions.assertThat(maybe.equals(maybe2)).isFalse();
		Assertions.assertThat(maybe2.equals(maybe)).isFalse();
	}

	@Test
	public void testToString() {
		Maybe<String> maybe = new Maybe<String>(null);
		Assertions.assertThat(maybe.toString()).doesNotContain(CONTENT);
		maybe = new Maybe<String>(CONTENT);
		Assertions.assertThat(maybe.toString()).contains(CONTENT);
	}

}
