/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.seedstack.seed.core.api.Logging;
import io.nuun.kernel.api.assertions.AssertUtils;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Guice type listener that will register any type having a field annotated with {@link Logging}.
 *
 * @author adrien.lauer@mpsa.com
 */
class LoggingTypeListener implements TypeListener {
    @Override
    public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
        for (Class<?> c = typeLiteral.getRawType(); c != Object.class; c = c.getSuperclass()) {
            for (Field field : typeLiteral.getRawType().getDeclaredFields()) {
                if (field.getType() == Logger.class && annotationPresent(field, Logging.class)) {
                    try {
                        typeEncounter.register(new LoggingMembersInjector<T>(field));
                    } catch (Exception e) {
                        // nothing to do here, exception will be thrown when the actual logger will be injected
                    }
                }
            }
        }
    }


    private boolean annotationPresent(Field field, Class<? extends Annotation> annoClass) {
        for (Annotation anno : field.getAnnotations()) {
            if (AssertUtils.hasAnnotationDeep(anno.annotationType(), annoClass)) {
                return true;
            }
        }

        return false;
    }
}
