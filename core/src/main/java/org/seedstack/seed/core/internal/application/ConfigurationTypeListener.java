/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.application;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.apache.commons.configuration.Configuration;

import java.lang.reflect.Field;

/**
 * Type listener for objects prepared for injection. Assigns injection policy
 * for each field annotated with {@link org.seedstack.seed.core.api.Configuration}.
 *
 * @author adrien.lauer@mpsa.com
 */
class ConfigurationTypeListener implements TypeListener {
    private final Configuration configuration;

    @Inject
    ConfigurationTypeListener(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <T> void hear(TypeLiteral<T> type, TypeEncounter<T> encounter) {
        for (Class<?> c = type.getRawType(); c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                org.seedstack.seed.core.api.Configuration anno = field.getAnnotation(org.seedstack.seed.core.api.Configuration.class);
                if (anno != null) {
                    encounter.register(new ConfigurationMembersInjector<T>(field, configuration, anno));
                }
            }
        }
    }
}
