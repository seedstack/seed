/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.jndi;

import static org.seedstack.shed.reflect.ReflectUtils.makeAccessible;

import com.google.inject.MembersInjector;
import java.lang.reflect.Field;
import javax.naming.Context;
import javax.naming.NamingException;
import org.seedstack.seed.SeedException;

/**
 * Custom field injector for JNDI resources.
 *
 * @param <T> The type to inject.
 */
class ResourceMembersInjector<T> implements MembersInjector<T> {
    private final Field field;
    private final Context context;
    private final String contextName;
    private final String resourceName;

    ResourceMembersInjector(Field field, Context context, String contextName, String resourceName) {
        this.field = makeAccessible(field);
        this.context = context;
        this.contextName = contextName;
        this.resourceName = resourceName;
    }

    @Override
    public void injectMembers(T t) {
        try {
            field.set(t, context.lookup(resourceName));
        } catch (NamingException | IllegalAccessException e) {
            throw SeedException.wrap(e, JndiErrorCode.UNABLE_TO_INJECT_JNDI_RESOURCE)
                    .put("field", field)
                    .put("context", contextName)
                    .put("resource", resourceName);
        }
    }
}