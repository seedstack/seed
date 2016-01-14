/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell.internal;

import com.google.inject.Module;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequestBuilder;
import org.apache.shiro.guice.ShiroModule;
import org.seedstack.seed.security.internal.SecurityProvider;

import java.util.Collection;
import java.util.Collections;

/**
 * Security plugin to integrate shell in security infrastructure.
 *
 * @author adrien.lauer@mpsa.com
 */
public class ShellSecurityProvider implements SecurityProvider {

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
        return new ShellSecurityModule();
    }
}