/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.jndi;

import static org.seedstack.shed.reflect.ReflectUtils.makeAccessible;

import com.google.inject.MembersInjector;
import java.lang.reflect.Field;

/**
 * Custom field injector for JNDI resources.
 *
 * @param <T> The type to inject.
 */
class ResourceMembersInjector<T> implements MembersInjector<T> {
    private final Field field;
    private final Object toInject;

    ResourceMembersInjector(Field field, Object toInject) {
        this.field = makeAccessible(field);
        this.toInject = toInject;
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