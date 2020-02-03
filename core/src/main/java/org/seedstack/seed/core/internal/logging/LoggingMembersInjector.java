/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.logging;

import static org.seedstack.shed.reflect.ReflectUtils.makeAccessible;

import com.google.inject.MembersInjector;
import java.lang.reflect.Field;
import java.util.Set;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.slf4j.LoggerFactory;

/**
 * Guice members injector that inject logger instances.
 *
 * @param <T> The type of class to inject.
 */
class LoggingMembersInjector<T> implements MembersInjector<T> {
    private final Set<Field> fields;

    LoggingMembersInjector(Set<Field> fields) {
        for (Field field : fields) {
            makeAccessible(field);
        }
        this.fields = fields;
    }

    @Override
    public void injectMembers(T t) {
        for (Field field : fields) {
            try {
                field.set(t, LoggerFactory.getLogger(field.getDeclaringClass()));
            } catch (IllegalAccessException e) {
                throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_INJECT_LOGGER).put("class",
                        field.getDeclaringClass().getCanonicalName()).put("field", field.getName());
            }
        }
    }
}