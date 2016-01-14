/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import org.apache.commons.configuration.Configuration;
import org.apache.shiro.mgt.SessionStorageEvaluator;
import org.apache.shiro.subject.Subject;

import javax.inject.Inject;
import javax.inject.Named;

class SeedSessionStorageEvaluator implements SessionStorageEvaluator {
    @Inject
    @Named("seed-security-config")
    private Configuration securityConfiguration;

    @Override
    public boolean isSessionStorageEnabled(Subject subject) {
        return securityConfiguration.getBoolean("sessions.enabled", false);
    }
}
