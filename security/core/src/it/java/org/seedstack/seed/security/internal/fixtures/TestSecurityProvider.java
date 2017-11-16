/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal.fixtures;

import com.google.inject.PrivateModule;
import com.google.inject.name.Names;
import io.nuun.kernel.core.AbstractPlugin;
import java.util.Collection;
import org.apache.shiro.guice.ShiroModule;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.seedstack.seed.security.internal.SecurityGuiceConfigurer;
import org.seedstack.seed.security.internal.SecurityProvider;

public class TestSecurityProvider extends AbstractPlugin implements SecurityProvider {
    @Override
    public String name() {
        return "test-security-provider";
    }

    @Override
    public PrivateModule provideMainSecurityModule(SecurityGuiceConfigurer securityGuiceConfigurer) {
        return null;
    }

    @Override
    public PrivateModule provideAdditionalSecurityModule() {
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
