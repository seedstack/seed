/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.Configuration;

/**
 * Guice type listener that will register any type having a field annotated with
 * {@link org.seedstack.seed.Configuration}.
 */
class ConfigurationTypeListener implements TypeListener {
    private final Coffig configuration;

    ConfigurationTypeListener(Coffig configuration) {
        this.configuration = configuration;
    }

    @Override
    public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
        Set<ConfigurationMembersInjector.ConfigurableField> fields = new HashSet<>();
        for (Class<?> c = typeLiteral.getRawType(); c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                Configuration annotation = field.getAnnotation(Configuration.class);
                if (annotation != null) {
                    fields.add(new ConfigurationMembersInjector.ConfigurableField(field, annotation));
                }
            }
        }

        if (!fields.isEmpty()) {
            typeEncounter.register(new ConfigurationMembersInjector<>(configuration, fields));
        }
    }
}
