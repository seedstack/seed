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

import com.google.inject.AbstractModule;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.core.api.Install;
import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.seed.security.api.Domain;
import org.seedstack.seed.security.api.SecuritySupport;
import org.seedstack.seed.security.api.WithUser;
import org.seedstack.seed.security.api.exceptions.AuthorizationException;
import org.seedstack.seed.security.api.principals.Principals;
import org.seedstack.seed.security.fixtures.AnnotatedClass4Security;

import javax.inject.Inject;
import javax.inject.Named;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SeedITRunner.class)
public class SecurityIT {

    @Inject
    private AnnotatedClass4Security annotatedClass;

    @Inject
    private SecuritySupport securitySupport;

    @Inject
    @Named("defaultSecurityManager")
    private org.apache.shiro.mgt.SecurityManager securityManager;

    @Test
    @WithUser(id = "Obiwan", password = "yodarulez")
    public void Obiwan_should_be_a_jedi() {
        assertThat(SecurityUtils.getSubject().hasRole("jedi")).isTrue();
        assertThat(securitySupport.hasRole("jedi")).isTrue();
        assertThat(securitySupport.hasRole("nothing")).isTrue();
    }

    @Test
    @WithUser(id = "Anakin", password = "imsodark")
    public void anakin_should_be_able_to_learn_at_the_academy_and_should_not_be_a_jedi() {
        assertThat(SecurityUtils.getSubject().isPermitted("academy:learn")).isTrue();
        assertThat(securitySupport.isPermitted("academy:learn")).isTrue();
        assertThat(SecurityUtils.getSubject().hasRole("jedi")).isFalse();
        assertThat(securitySupport.hasRole("jedi")).isFalse();
    }

    @Test(expected = AuthenticationException.class)
    public void user_zob_should_be_unknown() {
        Subject subject = new Subject.Builder(securityManager).buildSubject();
        UsernamePasswordToken token = new UsernamePasswordToken("zob", "");
        subject.login(token);
    }

    @Test
    @WithUser(id = "ThePoltergeist", password = "bouh")
    public void ThePoltergeist_should_be_ghost_on_MU() {
        assertThat(SecurityUtils.getSubject().hasRole("ghost")).isTrue();
        assertThat(securitySupport.hasRole("ghost")).isTrue();
        assertThat(securitySupport.hasRole("ghost", new Domain("MU"))).isTrue();
        assertThat(securitySupport.hasRole("ghost", new Domain("SX"))).isTrue();
        assertThat(securitySupport.isPermitted("site:haunt")).isTrue();
        assertThat(securitySupport.isPermitted("site:haunt", new Domain("MU"))).isTrue();
        assertThat(securitySupport.isPermitted("site:haunt", new Domain("SX"))).isTrue();
        assertThat(securitySupport.getDomains().contains(new Domain("MU"))).isTrue();
        assertThat(securitySupport.getDomains().contains(new Domain("SX"))).isTrue();
    }

    @Test
    @WithUser(id = "Obiwan", password = "yodarulez")
    public void Obiwan_should_be_able_to_call_the_force_and_teach() {
        assertThat(annotatedClass.callTheForce()).isTrue();
        assertThat(annotatedClass.teach()).isTrue();
    }

    @Test
    @WithUser(id = "nobody", password = "foreverAlone")
    public void user_nobody_should_have_role_nothing() {
        assertThat(securitySupport.hasRole("nothing")).isTrue();
    }

    @Test(expected = AuthorizationException.class)
    @WithUser(id = "Anakin", password = "imsodark")
    public void Anakin_should_not_be_able_to_call_the_force() {
        annotatedClass.callTheForce();
    }

    @Test(expected = AuthorizationException.class)
    @WithUser(id = "Anakin", password = "imsodark")
    public void Anakin_should_not_be_able_to_teach() {
        annotatedClass.teach();
    }

    @Test
    @WithUser(id = "Anakin", password = "imsodark")
    public void Anakin_should_have_customized_principal() {
        Assertions.assertThat(Principals.getSimplePrincipalByName(securitySupport.getOtherPrincipals(), "foo").getValue()).isEqualTo("bar");
    }

    @Install
    public static class securityTestModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(AnnotatedClass4Security.class);
        }

    }
}
