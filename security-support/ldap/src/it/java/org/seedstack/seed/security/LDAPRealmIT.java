/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.seed.it.api.WithPlugins;
import org.seedstack.seed.security.api.SecuritySupport;
import org.seedstack.seed.security.api.WithUser;
import org.seedstack.seed.security.ldap.api.LDAPException;
import org.seedstack.seed.security.ldap.api.LDAPSupport;
import org.seedstack.seed.security.ldap.api.LDAPUserContext;

@RunWith(SeedITRunner.class)
@WithPlugins(LDAPITPlugin.class)
public class LDAPRealmIT {

    @Inject
    @Named("defaultSecurityManager")
    private SecurityManager securityManager;

    @Inject
    private SecuritySupport securitySupport;

    @Inject
    private LDAPSupport ldapSupport;

    @Test
    @WithUser(id = "jdoe", password = "password")
    public void completeTest() throws LDAPException {
        assertThat(securitySupport.hasRole("jedi")).isTrue();
        LDAPUserContext userContext = securitySupport.getPrincipalsByType(LDAPUserContext.class).iterator().next().getPrincipal();
        assertThat(ldapSupport.getAttributeValue(userContext, "sn")).isEqualTo("jdoe");
        assertThat(ldapSupport.getAttributeValue(userContext, "dummy")).isNull();
    }

    @Test(expected = IncorrectCredentialsException.class)
    public void wrongPasswordTest() {
        ThreadContext.bind(securityManager);
        Subject subject = new Subject.Builder(securityManager).buildSubject();
        UsernamePasswordToken token = new UsernamePasswordToken("jdoe", "dummy");
        subject.login(token);
    }

    @Test(expected = UnknownAccountException.class)
    public void unknownUserTest() {
        ThreadContext.bind(securityManager);
        Subject subject = new Subject.Builder(securityManager).buildSubject();
        UsernamePasswordToken token = new UsernamePasswordToken("dummy", "password");
        subject.login(token);
    }
}
