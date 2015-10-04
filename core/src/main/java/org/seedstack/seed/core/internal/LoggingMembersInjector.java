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

import com.google.inject.MembersInjector;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.api.CoreErrorCode;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * Guice members injector that inject logger instances.
 *
 * @param <T> The type of class to inject.
 * @author adrien.lauer@mpsa.com
 */
class LoggingMembersInjector<T> implements MembersInjector<T> {
    private final Field field;

    LoggingMembersInjector(Field field) {
        this.field = field;
        field.setAccessible(true);
    }

    @Override
    public void injectMembers(T t) {
        try {
            field.set(t, LoggerFactory.getLogger(field.getDeclaringClass()));
        } catch (IllegalAccessException e) {
            throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_INJECT_LOGGER).put("class", field.getDeclaringClass().getCanonicalName()).put("field", field.getName());
        }
    }
}