/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import java.util.Optional;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.pam.AuthenticationStrategy;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SessionStorageEvaluator;
import org.apache.shiro.mgt.SubjectDAO;
import org.apache.shiro.mgt.SubjectFactory;
import org.apache.shiro.session.mgt.SessionValidationScheduler;
import org.apache.shiro.subject.SubjectContext;
import org.seedstack.seed.security.SecurityConfig;

public class SecurityGuiceConfigurer {
    private final SecurityConfig securityConfig;

    public SecurityGuiceConfigurer(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    public void configure(Binder binder) {
        // Subject
        SecurityConfig.SubjectConfig subjectConfig = securityConfig.subject();
        Optional.ofNullable(subjectConfig.getContext()).ifPresent(c -> binder.bind(SubjectContext.class).to(c));
        Optional.ofNullable(subjectConfig.getFactory()).ifPresent(f -> binder.bind(SubjectFactory.class).to(f));
        Class<? extends SubjectDAO> subjectDao = subjectConfig.getDao();
        binder.bind(SubjectDAO.class).to(subjectDao != null ? subjectDao : DefaultSubjectDAO.class);

        // Authentication
        SecurityConfig.AuthenticationConfig authenticationConfig = securityConfig.authentication();
        binder.bind(Authenticator.class).to(authenticationConfig.getAuthenticator());
        binder.bind(AuthenticationStrategy.class).to(authenticationConfig.getStrategy());
        binder.bind(CredentialsMatcher.class).to(authenticationConfig.getCredentialsMatcher());

        // Cache configuration
        SecurityConfig.CacheConfig cacheConfig = securityConfig.cache();
        binder.bind(CacheManager.class).to(cacheConfig.getManager());

        // Sessions
        SecurityConfig.SessionConfig sessionConfig = securityConfig.sessions();
        binder.bind(SessionStorageEvaluator.class).to(sessionConfig.getStorageEvaluator());
        Optional.ofNullable(sessionConfig.getValidationScheduler())
                .ifPresent(s -> binder.bind(SessionValidationScheduler.class).to(s));
        binder.bindConstant()
                .annotatedWith(Names.named("shiro.sessionValidationInterval"))
                .to(sessionConfig.getValidationInterval() * 1000);
        binder.bindConstant()
                .annotatedWith(Names.named("shiro.globalSessionTimeout"))
                .to(sessionConfig.getTimeout() * 1000);
    }
}
