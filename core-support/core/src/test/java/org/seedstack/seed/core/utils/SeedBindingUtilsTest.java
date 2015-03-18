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
package org.seedstack.seed.core.utils;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.seedstack.seed.core.api.SeedException;
import org.junit.Test;
import org.seedstack.seed.core.assertions.BindingDefinitionsAssert;
import org.seedstack.seed.core.utils.sample.*;

import static org.seedstack.seed.core.utils.SeedBindingUtils.resolveBindingDefinitions;

/**
 * 
 * @author redouane.loulou@ext.mpsa.com
 */
public class SeedBindingUtilsTest {

	@Test
	public void binding_definitions_on_interface_injectee_with_type_variable_should_work() {
		BindingDefinitionsAssert.assertBindingDefinitions(IType.class, StringType.class, IntegerType.class)
				.keyIsAssociatedTo(Key.get(new TypeLiteral<IType<String>>() {
				}), StringType.class).keyIsAssociatedTo(Key.get(new TypeLiteral<IType<Integer>>() {
				}), IntegerType.class);
	}

	@Test
	public void binding_definitions_on_injectee_with_raw_type_should_work() {
		BindingDefinitionsAssert.assertBindingDefinitions(Object.class, String.class).keyIsAssociatedTo(Key.get(new TypeLiteral<Object>() {
		}), String.class);
	}

	@Test
	public void binding_definitions_on_class_injectee_with_type_variable_should_work() {
		BindingDefinitionsAssert.assertBindingDefinitions(TestType.class, ObjectStringTestType.class, ObjectIntegerTestType.class).keyIsAssociatedTo(
				Key.get(new TypeLiteral<TestType<Object, String>>() {
				}), ObjectStringTestType.class).keyIsAssociatedTo(Key.get(new TypeLiteral<TestType<Object, Integer>>() {
		}), ObjectIntegerTestType.class);
	}

	@Test(expected = SeedException.class)
	public void binding_definitions_with_duplicate_keys_via_typevariable_should_not_work() {
		resolveBindingDefinitions(TestType.class, ObjectStringTestType.class, (new TestType<Object, String>() {
		}).getClass(), ObjectIntegerTestType.class);
	}
	
	@Test(expected = SeedException.class)
	public void binding_definitions_with_duplicate_keys_via_qualifier_should_not_work() {
		resolveBindingDefinitions(TestType.class, CollectionQualifiedTestType2.class, CollectionQualifiedTestType2.class);
	}

}
