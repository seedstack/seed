/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal.realms;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.security.AuthenticationInfo;
import org.seedstack.seed.security.AuthenticationToken;
import org.seedstack.seed.security.IncorrectCredentialsException;
import org.seedstack.seed.security.SecurityConfig;
import org.seedstack.seed.security.UnknownAccountException;
import org.seedstack.seed.security.UnsupportedTokenException;
import org.seedstack.seed.security.UsernamePasswordToken;
import org.seedstack.seed.security.internal.realms.ConfigurationRealm.ConfigurationUser;
import org.seedstack.seed.security.principals.PrincipalProvider;
import org.seedstack.seed.security.principals.Principals;

public class ConfigurationRealmUnitTest {
    private String username = "username";
    private String password = "password";
    private String role1 = "role1";
    private String role2 = "role2";
    private ConfigurationRealm underTest;
    private Set<ConfigurationUser> users;

    @Before
    public void before() {
        underTest = new ConfigurationRealm(null, null);
        users = Deencapsulation.getField(underTest, "users");
        users.add(new ConfigurationUser(username, password, Sets.newHashSet(role1, role2)));
        users.add(new ConfigurationUser("toto", null, Sets.newHashSet()));
    }

    @Test
    public void getRealmRoles_nominal() {
        PrincipalProvider<String> identity = Principals.identityPrincipal(username);

        Set<String> foundRoles = underTest.getRealmRoles(identity, Collections.emptyList());

        assertThat(foundRoles).hasSize(2);
        assertThat(foundRoles).contains(role1);
        assertThat(foundRoles).contains(role2);
    }

    @Test
    public void getRealmRoles_returns_empty_if_user_unknown() {
        String username = "titi";
        PrincipalProvider<String> identity = Principals.identityPrincipal(username);

        Set<String> foundRoles = underTest.getRealmRoles(identity, Collections.emptyList());

        assertThat(foundRoles).isEmpty();
    }

    @Test
    public void getAuthenticationInfo_nominal() {
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        AuthenticationInfo authInfo = underTest.getAuthenticationInfo(token);
        assertThat(authInfo.getIdentityPrincipal().getPrincipal()).isEqualTo("username");
    }

    @Test(expected = IncorrectCredentialsException.class)
    public void getAuthenticationInfo_throws_exception_if_incorrect_password() {
        UsernamePasswordToken token = new UsernamePasswordToken(username, "");
        underTest.getAuthenticationInfo(token);
    }

    @Test(expected = UnknownAccountException.class)
    public void getAuthenticationInfo_throws_exception_if_unknown_user() {
        UsernamePasswordToken token = new UsernamePasswordToken("", password);
        underTest.getAuthenticationInfo(token);
    }

    @Test(expected = NullPointerException.class)
    public void null_user_is_invalid() {
        new UsernamePasswordToken(null, password);
    }

    @Test(expected = NullPointerException.class)
    public void null_password_is_invalid() {
        UsernamePasswordToken token = new UsernamePasswordToken(username, (String) null);
        underTest.getAuthenticationInfo(token);
    }

    @Test(expected = UnsupportedTokenException.class)
    public void getAuthenticationInfo_throws_exception_if_token_not_compliant() {
        AuthenticationToken token = new AuthenticationToken() {
            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }
        };
        underTest.getAuthenticationInfo(token);
    }

    @Test
    public void readConfiguration_empty_props() {
        underTest.readConfiguration(new SecurityConfig());
    }

    @Test
    public void readConfiguration_with_users() {
        SecurityConfig securityConfig = new SecurityConfig()
                .addUser("Obiwan", new SecurityConfig.UserConfig().setPassword("yodarulez").addRole("SEED.JEDI"))
                .addUser("Anakin", new SecurityConfig.UserConfig().setPassword("imsodark"));

        underTest.readConfiguration(securityConfig);
        assertThat(users).hasSize(2);
        assertThat(users).containsOnly(new ConfigurationUser("Obiwan"), new ConfigurationUser("Anakin"));
    }
}
