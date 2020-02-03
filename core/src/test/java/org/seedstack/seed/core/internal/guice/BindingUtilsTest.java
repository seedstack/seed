/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */


package org.seedstack.seed.core.internal.guice;

import static org.seedstack.seed.core.internal.guice.BindingUtils.resolveBindingDefinitions;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.guice.sample.CollectionQualifiedTestType2;
import org.seedstack.seed.core.internal.guice.sample.IType;
import org.seedstack.seed.core.internal.guice.sample.IntegerType;
import org.seedstack.seed.core.internal.guice.sample.ObjectIntegerTestType;
import org.seedstack.seed.core.internal.guice.sample.ObjectStringTestType;
import org.seedstack.seed.core.internal.guice.sample.StringType;
import org.seedstack.seed.core.internal.guice.sample.TestType;

public class BindingUtilsTest {
    /**
     * Assert on the {@link BindingUtils#resolveBindingDefinitions(Class, Class, Class[])} )} method.
     *
     * @param injecteeClass the parent class to find
     * @param implClasses   the associated class to bind
     * @return the BindableKeyMapProvider of the DSL
     */
    @SuppressWarnings("unchecked")
    private static <T> KeyAssociationProvider assertBindingDefinitions(Class<T> injecteeClass,
            Class<? extends T>... implClasses) {
        return new KeyAssociationProvider<>(resolveBindingDefinitions(injecteeClass, null, implClasses));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void binding_definitions_on_interface_injectee_with_type_variable_should_work() {
        assertBindingDefinitions(IType.class, StringType.class, IntegerType.class)
                .keyIsAssociatedTo(Key.get(new TypeLiteral<IType<String>>() {
                }), StringType.class).keyIsAssociatedTo(Key.get(new TypeLiteral<IType<Integer>>() {
        }), IntegerType.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void binding_definitions_on_injectee_with_raw_type_should_work() {
        assertBindingDefinitions(Object.class, String.class).keyIsAssociatedTo(Key.get(new TypeLiteral<Object>() {
        }), String.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void binding_definitions_on_class_injectee_with_type_variable_should_work() {
        assertBindingDefinitions(TestType.class, ObjectStringTestType.class,
                ObjectIntegerTestType.class).keyIsAssociatedTo(
                Key.get(new TypeLiteral<TestType<Object, String>>() {
                }), ObjectStringTestType.class).keyIsAssociatedTo(Key.get(new TypeLiteral<TestType<Object, Integer>>() {
        }), ObjectIntegerTestType.class);
    }

    @Test(expected = SeedException.class)
    @SuppressWarnings("unchecked")
    public void binding_definitions_with_duplicate_keys_via_typevariable_should_not_work() {
        resolveBindingDefinitions(TestType.class, ObjectStringTestType.class, (new TestType<Object, String>() {
        }).getClass(), ObjectIntegerTestType.class);
    }

    @Test(expected = SeedException.class)
    @SuppressWarnings("unchecked")
    public void binding_definitions_with_duplicate_keys_via_qualifier_should_not_work() {
        resolveBindingDefinitions(TestType.class, CollectionQualifiedTestType2.class,
                CollectionQualifiedTestType2.class);
    }

    /**
     * This class provides method to test the returned bindable map.
     */
    private static class KeyAssociationProvider<T> {
        private Map<Key<T>, Class<? extends T>> bindingDefinitions;

        KeyAssociationProvider(Map<Key<T>, Class<? extends T>> bindingDefinitions) {
            this.bindingDefinitions = bindingDefinitions;
        }

        /**
         * Check if the key is associated to the given subclass.
         *
         * @param key       the key
         * @param implClass the expected subclass
         * @return this
         */
        KeyAssociationProvider keyIsAssociatedTo(Key<T> key, Class<? extends T> implClass) {
            Assertions.assertThat(bindingDefinitions.get(key)).isEqualTo(implClass);
            return this;
        }
    }
}
