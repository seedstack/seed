/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.fixtures;

import com.google.inject.Module;
import com.google.inject.name.Names;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequestBuilder;
import org.apache.shiro.guice.ShiroModule;
import org.apache.shiro.mgt.*;
import org.apache.shiro.mgt.SecurityManager;
import org.seedstack.seed.security.internal.SecurityProvider;

import java.util.Collection;

public class AdditionalSecurityProvider implements SecurityProvider {
    @Override
    public void init(InitContext initContext) {
        // nothing to do
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        // nothing to do
    }

    @Override
    public void classpathScanRequests(ClasspathScanRequestBuilder classpathScanRequestBuilder) {
        // nothing to do
    }

    @Override
    public Module provideMainSecurityModule() {
        return null;
    }

    @Override
    public Module provideAdditionalSecurityModule() {
        return new ShiroModule() {
            @Override
            protected void configureShiro() {
                try {
                    bind(org.apache.shiro.mgt.SecurityManager.class)
                            .annotatedWith(Names.named("test"))
                            .toConstructor(DefaultSecurityManager.class.getConstructor(Collection.class))
                            .asEagerSingleton();
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Internal error", e);
                }

                expose(SecurityManager.class).annotatedWith(Names.named("test"));
            }
        };
    }
}
