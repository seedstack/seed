/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
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
import javax.naming.NamingException;
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
                    Context contextToLookup = defaultContext;
                    JndiContext jndiContextAnnotation = field.getAnnotation(JndiContext.class);
                    if (jndiContextAnnotation != null) {
                        contextToLookup = jndiContexts.get(jndiContextAnnotation.value());
                    }

                    String resourceName = resourceAnnotation.name();
                    if (!resourceName.isEmpty()) {
                        try {
                            typeEncounter.register(
                                    new ResourceMembersInjector<>(field, contextToLookup.lookup(resourceName)));
                        } catch (NamingException e) {
                            String contextName = "default";
                            if (jndiContextAnnotation != null) {
                                contextName = jndiContextAnnotation.value();
                            }

                            throw SeedException.wrap(e, JndiErrorCode.UNABLE_TO_REGISTER_INJECTION_FOR_RESOURCE)
                                    .put("field", field.getName())
                                    .put("class", c.getCanonicalName())
                                    .put("resource", resourceName)
                                    .put("context", contextName);
                        }
                    }
                }
            }
        }
    }
}
