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

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import org.seedstack.seed.security.api.annotations.RequiresPermissions;
import org.seedstack.seed.security.api.annotations.RequiresRoles;
import org.seedstack.seed.security.internal.authorization.RequiresPermissionsInterceptor;
import org.seedstack.seed.security.internal.authorization.RequiresRolesInterceptor;

class SecurityAopModule extends AbstractModule {
    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(RequiresRoles.class), new RequiresRolesInterceptor(new ShiroSecuritySupport()));
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(RequiresPermissions.class), new RequiresPermissionsInterceptor(new ShiroSecuritySupport()));
    }
}
