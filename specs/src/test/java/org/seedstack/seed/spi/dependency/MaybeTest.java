/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.spi.dependency;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Unit test for {@link Maybe}.
 *
 * @author thierry.bouvet@mpsa.com
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
	public void testEmpty() {
        Assertions.assertThat(Maybe.<String>empty()).isEqualTo(new Maybe<String>(null));
    }

    @Test
    public void testOf() {
        Assertions.assertThat(Maybe.of("toto")).isEqualTo(new Maybe<String>("toto"));
    }

	@Test
	public void testEqualsObject() {
		Maybe<String> emptyMaybe = new Maybe<String>(null);
		Assertions.assertThat(emptyMaybe.equals(CONTENT)).isFalse();
		Assertions.assertThat(emptyMaybe.equals(null)).isFalse();

        Maybe<String> dummyMaybe = new Maybe<String>(CONTENT);
		Assertions.assertThat(dummyMaybe.equals(dummyMaybe)).isTrue();
		Maybe<String> emptyMaybe2 = new Maybe<String>(null);
		Assertions.assertThat(dummyMaybe.equals(emptyMaybe2)).isFalse();
		Assertions.assertThat(emptyMaybe2.equals(dummyMaybe)).isFalse();

        Assertions.assertThat(emptyMaybe2.equals(emptyMaybe)).isTrue();
	}

	@Test
	public void testToString() {
		Maybe<String> maybe = new Maybe<String>(null);
		Assertions.assertThat(maybe.toString()).doesNotContain(CONTENT);
		maybe = new Maybe<String>(CONTENT);
		Assertions.assertThat(maybe.toString()).contains(CONTENT);
	}

}
