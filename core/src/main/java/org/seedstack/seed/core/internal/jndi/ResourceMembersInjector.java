/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.jndi;

import com.google.inject.MembersInjector;

import java.lang.reflect.Field;

/**
 * Custom field injector for JNDI resources.
 *
 * @param <T>
 *         The type to inject.
 * @author adrien.lauer@mpsa.com
 */
class ResourceMembersInjector<T> implements MembersInjector<T> {
    private final Field field;
    private final Object toInject;

    ResourceMembersInjector(Field field, Object toInject) {
        this.field = field;
        this.toInject = toInject;
        field.setAccessible(true);
    }

    @Override
    public void injectMembers(T t) {
        try {
            field.set(t, this.toInject);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}