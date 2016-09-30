/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.inject.MembersInjector;
import org.seedstack.shed.exception.SeedException;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Guice members injector that inject logger instances.
 *
 * @param <T> The type of class to inject.
 * @author adrien.lauer@mpsa.com
 */
class LoggingMembersInjector<T> implements MembersInjector<T> {
    private final Set<Field> fields;

    LoggingMembersInjector(Set<Field> fields) {
        this.fields = fields;
        for (Field field : fields) {
            field.setAccessible(true);
        }
    }

    @Override
    public void injectMembers(T t) {
        for (Field field : fields) {
            try {
                field.set(t, LoggerFactory.getLogger(field.getDeclaringClass()));
            } catch (IllegalAccessException e) {
                throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_INJECT_LOGGER).put("class", field.getDeclaringClass().getCanonicalName()).put("field", field.getName());
            }
        }
    }
}