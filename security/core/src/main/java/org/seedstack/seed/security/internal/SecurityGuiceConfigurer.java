/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SessionStorageEvaluator;
import org.apache.shiro.mgt.SubjectDAO;

public class SecurityGuiceConfigurer {
    public static final long DEFAULT_GLOBAL_SESSION_TIMEOUT = 1000 * 60 * 15;

    private final Configuration securityConfiguration;

    public SecurityGuiceConfigurer(Configuration securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
    }

    public void configure(Binder binder) {
        binder.bind(SubjectDAO.class).to(DefaultSubjectDAO.class);
        binder.bind(SessionStorageEvaluator.class).to(SeedSessionStorageEvaluator.class);
        binder.bind(CacheManager.class).to(MemoryConstrainedCacheManager.class);
        binder.bindConstant().annotatedWith(Names.named("shiro.globalSessionTimeout")).to(securityConfiguration.getLong("sessions.timeout", DEFAULT_GLOBAL_SESSION_TIMEOUT));
    }
}
