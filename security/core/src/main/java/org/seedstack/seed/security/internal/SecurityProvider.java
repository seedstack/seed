/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import com.google.inject.PrivateModule;
import io.nuun.kernel.api.annotations.Facet;

/**
 * A plugin to augment security in any entry point while original security
 * plugin is loaded by kernel.
 */
@Facet
public interface SecurityProvider {

    /**
     * Provides the Guice module for handling the main application security. There can be only one main Guice module
     * in an application.
     *
     * @return the Guice module for the application main security.
     */
    PrivateModule provideMainSecurityModule(SecurityGuiceConfigurer securityGuiceConfigurer);

    /**
     * Provides a Guice module for handling additional entry point security. Multiple entry-point-specific Guice modules
     * are possible.
     *
     * @return the Guice module for the specific entry-point.
     */
    PrivateModule provideAdditionalSecurityModule();

}