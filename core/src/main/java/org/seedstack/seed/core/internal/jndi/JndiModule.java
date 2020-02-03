/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.jndi;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import java.util.Map;
import javax.naming.Context;

/**
 * Guice module that binds the configured JNDI context.
 */
class JndiModule extends AbstractModule {
    private Map<String, Context> jndiContextsToBeBound;
    private Context defaultContext;

    JndiModule(Context defaultContext, Map<String, Context> jndiContextsToBeBound) {
        this.jndiContextsToBeBound = jndiContextsToBeBound;
        this.defaultContext = defaultContext;
    }

    @Override
    protected void configure() {
        requestStaticInjection(JndiContext.class);

        // Bind default context
        Key<Context> defaultContextKey = Key.get(Context.class, Names.named("defaultContext"));
        bind(defaultContextKey).toInstance(this.defaultContext);
        bind(Context.class).to(defaultContextKey);

        // Bind additional contexts
        for (Map.Entry<String, Context> jndiContextToBeBound : jndiContextsToBeBound.entrySet()) {
            Key<Context> key = Key.get(Context.class, Names.named(jndiContextToBeBound.getKey()));
            bind(key).toInstance(jndiContextToBeBound.getValue());
        }

        bindListener(Matchers.any(), new ResourceTypeListener(this.defaultContext, this.jndiContextsToBeBound));
    }
}
