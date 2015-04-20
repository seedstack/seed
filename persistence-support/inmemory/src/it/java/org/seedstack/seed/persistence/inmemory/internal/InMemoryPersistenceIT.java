/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */
package org.seedstack.seed.persistence.inmemory.internal;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.seed.persistence.inmemory.api.InMemory;
import org.seedstack.seed.persistence.inmemory.api.Store;

import java.util.Map;

/**
 * InMemoryPersistenceIT
 *
 * @author redouane.loulou@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 */
@RunWith(SeedITRunner.class)
public class InMemoryPersistenceIT {
	@InMemory
	Map<String,String> inMemorySortedMap;


	@Test  @Store("test1")
	public void test1() {
		Assertions.assertThat(inMemorySortedMap).isNotNull();
		inMemorySortedMap.put("1", "test1");
		inMemorySortedMap.put("2", "test2");
		Assertions.assertThat(inMemorySortedMap.get("1")).isEqualTo("test1");
	}

	@Test @Store("test2")
	public void test3() {
		Assertions.assertThat(inMemorySortedMap.get("1")).isNull();
		inMemorySortedMap.put("1", "test3");
		Assertions.assertThat(inMemorySortedMap.get("1")).isEqualTo("test3");
		inMemorySortedMap.remove("1");
		Assertions.assertThat(inMemorySortedMap.get("1")).isNull();
	}
}
