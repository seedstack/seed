/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SessionStorageEvaluator;
import org.apache.shiro.mgt.SubjectDAO;
import org.seedstack.seed.security.SecurityConfig;

public class SecurityGuiceConfigurer {
    private final SecurityConfig securityConfig;

    public SecurityGuiceConfigurer(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    public void configure(Binder binder) {
        binder.bind(SubjectDAO.class).to(DefaultSubjectDAO.class);
        binder.bind(SessionStorageEvaluator.class).to(SeedSessionStorageEvaluator.class);
        binder.bind(CacheManager.class).to(MemoryConstrainedCacheManager.class);
        binder.bindConstant().annotatedWith(Names.named("shiro.globalSessionTimeout")).to(
                securityConfig.sessions().getTimeout());
    }
}
