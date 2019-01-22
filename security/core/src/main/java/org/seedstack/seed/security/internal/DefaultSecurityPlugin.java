/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import com.google.inject.PrivateModule;
import io.nuun.kernel.core.AbstractPlugin;

public class DefaultSecurityPlugin extends AbstractPlugin implements SecurityProvider {
    @Override
    public String name() {
        return "default-security";
    }

    @Override
    public PrivateModule provideMainSecurityModule(SecurityGuiceConfigurer securityGuiceConfigurer) {
        return new DefaultSecurityModule(securityGuiceConfigurer);
    }

    @Override
    public PrivateModule provideAdditionalSecurityModule() {
        return null;
    }
}
