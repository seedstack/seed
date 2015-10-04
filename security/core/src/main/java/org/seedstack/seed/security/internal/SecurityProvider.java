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

import java.util.Collection;

/**
 * A plugin to augment security in any entry point while original security
 * plugin is loaded by kernel.
 *
 * @author yves.dautremay@mpsa.com
 */
public interface SecurityProvider {

    /**
     * Init phase.
     *
     * @param initContext The init context.
     */
    void init(InitContext initContext);

    /**
     * If container context is needed (web environment)
     *
     * @param containerContext the container context
     */
    void provideContainerContext(Object containerContext);

    /**
     * Requests for classpath scan
     *
     * @param classpathScanRequestBuilder the request builder to address the requests
     */
    void classpathScanRequests(ClasspathScanRequestBuilder classpathScanRequestBuilder);

    /**
     * Provides the Guice module for handling the main application security. There can be only one main Guice module
     * in an application.
     *
     * @return the Guice module for the application main security.
     */
    Module provideMainSecurityModule();

    /**
     * Provides a Guice module for handling additional entry point security. Multiple entry-point-specific Guice modules
     * are possible.
     *
     * @return the Guice module for the specific entry-point.
     */
    Module provideAdditionalSecurityModule();
}