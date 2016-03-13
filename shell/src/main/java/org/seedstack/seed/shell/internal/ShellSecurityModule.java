/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell.internal;

import com.google.inject.name.Names;
import org.apache.shiro.guice.ShiroModule;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;

import java.util.Collection;

class ShellSecurityModule extends ShiroModule {
    private static final String SHELL_SECURITY_MANAGER = "shell";

    @Override
    protected void configureShiro() {
        try {
            bind(SecurityManager.class)
                    .annotatedWith(Names.named(SHELL_SECURITY_MANAGER))
                    .toConstructor(DefaultSecurityManager.class.getConstructor(Collection.class))
                    .asEagerSingleton();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Internal error", e);
        }

        expose(SecurityManager.class).annotatedWith(Names.named(SHELL_SECURITY_MANAGER));
    }
}
