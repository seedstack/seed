/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.logging;

import static java.lang.String.format;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.seedstack.seed.Logging;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.slf4j.Logger;

/**
 * Guice type listener that will register any type having a field annotated with {@link Logging}.
 */
class LoggingTypeListener implements TypeListener {
    @Override
    public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
        Set<Field> fields = new HashSet<>();
        for (Class<?> c = typeLiteral.getRawType(); c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (field.isAnnotationPresent(Logging.class)) {
                    if (field.getType() == Logger.class) {
                        fields.add(field);
                    } else {
                        throw SeedException.createNew(CoreErrorCode.BAD_LOGGER_TYPE)
                                .put("field", format("%s.%s", field.getDeclaringClass().getName(), field.getName()))
                                .put("expectedType", Logger.class.getName())
                                .put("givenType", field.getType().getName());
                    }
                }
            }
        }

        if (!fields.isEmpty()) {
            typeEncounter.register(new LoggingMembersInjector<>(fields));
        }
    }
}
