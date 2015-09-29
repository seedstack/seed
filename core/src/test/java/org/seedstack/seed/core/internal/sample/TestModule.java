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
package org.seedstack.seed.core.internal.sample;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.TypeLiteral;
import org.seedstack.seed.core.internal.AbstractSeedModule;
import org.seedstack.seed.core.utils.sample.CollectionQualifiedTestType;
import org.seedstack.seed.core.utils.sample.CollectionQualifiedTestType2;
import org.seedstack.seed.core.utils.sample.CollectionTestType;
import org.seedstack.seed.core.utils.sample.TestType;

import java.util.Collection;

/**
 * TestModule
 * 
 * @author redouane.loulou@ext.mpsa.com
 * 
 */

public class TestModule extends AbstractSeedModule {

	@Override
	protected void configure() {
		Multimap<TypeLiteral<?>, Class<?>> multimap = ArrayListMultimap.create();
		multimap.put(new TypeLiteral<TestType<Collection<String>, Collection<Collection<Long>>>>() {
		}, CollectionTestType.class);
		multimap.put(new TypeLiteral<TestType<Collection<String>, String>>() {
		}, CollectionQualifiedTestType2.class);
		multimap.put(new TypeLiteral<TestType<Collection<String>, Collection<Collection<Long>>>>() {
		}, CollectionQualifiedTestType.class);
		bindTypeLiterals(multimap);

	}

}
