/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import static org.seedstack.shed.reflect.Classes.instantiateDefault;
import static org.seedstack.shed.reflect.ReflectUtils.makeAccessible;

import com.google.common.base.Joiner;
import com.google.inject.MembersInjector;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;

/**
 * Guice members injector that inject logger instances.
 *
 * @param <T> The type of class to inject.
 */
class ConfigurationMembersInjector<T> implements MembersInjector<T> {
    private final Set<ConfigurableField> fields;
    private final Coffig coffig;

    ConfigurationMembersInjector(Coffig coffig, Set<ConfigurableField> fields) {
        this.coffig = coffig;
        this.fields = fields;
    }

    @Override
    public void injectMembers(T t) {
        for (ConfigurableField configurableField : fields) {
            Configuration configuration = configurableField.getConfiguration();
            Field field = configurableField.getField();
            Class<?> fieldType = field.getType();
            Optional<?> optionalValue = coffig.getOptional(fieldType, configuration.value());

            try {
                if (optionalValue.isPresent()) {
                    field.set(t, optionalValue.get());
                } else if (configuration.mandatory()) {
                    throw SeedException.createNew(CoreErrorCode.MISSING_CONFIGURATION_KEY)
                            .put("key", computeConfigKey(configuration, fieldType));
                } else if (field.get(t) == null && configuration.injectDefault()) {
                    field.set(t, instantiateDefault(fieldType));
                }
            } catch (IllegalAccessException e) {
                throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_INJECT_CONFIGURATION_VALUE)
                        .put("class", field.getDeclaringClass().getCanonicalName())
                        .put("field", field.getName())
                        .put("key", computeConfigKey(configuration, fieldType));
            }
        }
    }

    private String computeConfigKey(Configuration configuration, Class<?> fieldType) {
        return configuration.value().length > 0 ? Joiner.on(".").join(configuration.value()) : Coffig.pathOf(fieldType);
    }

    static class ConfigurableField {
        private final Field field;
        private final Configuration configuration;

        ConfigurableField(Field field, Configuration configuration) {
            this.field = makeAccessible(field);
            this.configuration = configuration;
        }

        Field getField() {
            return field;
        }

        Configuration getConfiguration() {
            return configuration;
        }
    }
}