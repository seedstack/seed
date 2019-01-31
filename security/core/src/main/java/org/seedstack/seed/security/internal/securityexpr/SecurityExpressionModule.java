/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.securityexpr;

import com.google.inject.AbstractModule;

public class SecurityExpressionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SecurityExpressionInterpreter.class);

        // we inject the security support
        requestStaticInjection(SecurityExpressionUtils.class);

        // TODO : add the SPI for Security Expression
    }

}
