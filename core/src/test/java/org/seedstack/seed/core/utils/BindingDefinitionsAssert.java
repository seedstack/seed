/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

import com.google.inject.Key;
import org.assertj.core.api.Assertions;

import java.util.Map;

import static org.seedstack.seed.core.utils.SeedBindingUtils.resolveBindingDefinitions;

/**
 * This assertion class provides assertions around bindings.
 *  
 * @author pierre.thirouin@ext.mpsa.com
 */
public class BindingDefinitionsAssert {

    /**
     * Asserts on the {@link org.seedstack.seed.core.utils.SeedBindingUtils#resolveBindingDefinitions(Class, Class, Class[])} method.
     * 
     * @param injecteeClass the parent class to find
     * @param implClasses the associated class to bind
     * @return the BindableKeyMapProvider of the DSL 
     */
    public static KeyAssociationProvider assertBindingDefinitions(Class<?> injecteeClass, Class<?>... implClasses) {
        return new KeyAssociationProvider(resolveBindingDefinitions(injecteeClass, null , implClasses));
    }

    /**
     * This class provides method to test the returned bindable map.
     */
    public static class KeyAssociationProvider {

        Map<Key<?>, Class<?>> bindingDefinitions;

        KeyAssociationProvider(Map<Key<?>, Class<?>> bindingDefinitions) {
            this.bindingDefinitions = bindingDefinitions;
        }

        /**
         * Check if the key is associated to the given subclass.
         *
         * @param key the key
         * @param implClass the expected subclass
         * @return this
         */
        public KeyAssociationProvider keyIsAssociatedTo(Key<?> key, Class<?> implClass) {
            Assertions.assertThat(bindingDefinitions.get(key)).isEqualTo(implClass);
            return this;
        }
    }
}
