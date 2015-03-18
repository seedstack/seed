/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import com.google.inject.Module;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequestBuilder;
import org.apache.shiro.guice.ShiroModule;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A default security plugin that does nothing
 * 
 * @author yves.dautremay@mpsa.com
 * 
 */
public class DefaultSecurityPlugin implements SeedSecurityPlugin {

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
    public Collection<Module> provideOtherModules() {
        return new ArrayList<Module>();
    }

    @Override
    public ShiroModule provideShiroModule() {
        return new DefaultShiroModule();
    }
}
