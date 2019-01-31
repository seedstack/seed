/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.jndi;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import java.lang.reflect.Field;
import java.util.Map;
import javax.annotation.Resource;
import javax.naming.Context;
import org.seedstack.seed.JndiContext;
import org.seedstack.seed.SeedException;

/**
 * Guice type listener for {@link Resource} annotated fields.
 */
class ResourceTypeListener implements TypeListener {
    private Map<String, Context> jndiContexts;
    private Context defaultContext;

    ResourceTypeListener(Context defaultContext, Map<String, Context> jndiContexts) {
        this.defaultContext = defaultContext;
        this.jndiContexts = jndiContexts;
    }

    @Override
    public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
        for (Class<?> c = typeLiteral.getRawType(); c != Object.class; c = c.getSuperclass()) {
            for (Field field : typeLiteral.getRawType().getDeclaredFields()) {
                Resource resourceAnnotation = field.getAnnotation(Resource.class);
                if (resourceAnnotation != null) {
                    String resourceName = resourceAnnotation.name();
                    Context contextToLookup = defaultContext;
                    String contextName = "default";

                    // Check if this injection is from an additional context
                    JndiContext jndiContextAnnotation = field.getAnnotation(JndiContext.class);
                    if (jndiContextAnnotation != null) {
                        contextName = jndiContextAnnotation.value();
                        contextToLookup = jndiContexts.get(contextName);
                        if (contextToLookup == null) {
                            throw SeedException.createNew(JndiErrorCode.UNKNOWN_JNDI_CONTEXT)
                                    .put("field", field)
                                    .put("context", contextName);
                        }
                    }

                    // Register the members injector
                    if (!resourceName.isEmpty()) {
                        typeEncounter.register(new ResourceMembersInjector<>(field,
                                contextToLookup,
                                contextName,
                                resourceName));
                    }
                }
            }
        }
    }
}
