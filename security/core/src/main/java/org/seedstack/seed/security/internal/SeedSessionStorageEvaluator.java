/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal;

import javax.inject.Inject;
import org.apache.shiro.mgt.SessionStorageEvaluator;
import org.apache.shiro.subject.Subject;
import org.seedstack.seed.security.SecurityConfig;

class SeedSessionStorageEvaluator implements SessionStorageEvaluator {
    @Inject
    private SecurityConfig securityConfig;

    @Override
    public boolean isSessionStorageEnabled(Subject subject) {
        return securityConfig.sessions().isEnabled();
    }
}
