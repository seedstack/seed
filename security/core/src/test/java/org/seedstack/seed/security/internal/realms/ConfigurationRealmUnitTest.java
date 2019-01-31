/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.realms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.security.AuthenticationInfo;
import org.seedstack.seed.security.AuthenticationToken;
import org.seedstack.seed.security.IncorrectCredentialsException;
import org.seedstack.seed.security.SecurityConfig;
import org.seedstack.seed.security.UnknownAccountException;
import org.seedstack.seed.security.UnsupportedTokenException;
import org.seedstack.seed.security.UsernamePasswordToken;
import org.seedstack.seed.security.principals.PrincipalProvider;
import org.seedstack.seed.security.principals.Principals;

public class ConfigurationRealmUnitTest {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String ROLE_1 = "role1";
    private static final String ROLE_2 = "role2";
    private ConfigurationRealm underTest;

    @Before
    public void before() {
        underTest = new ConfigurationRealm(null, null, new SecurityConfig()
                .addUser(USERNAME,
                        new SecurityConfig.UserConfig().setPassword(PASSWORD).addRole(ROLE_1).addRole(ROLE_2))
                .addUser("toto", new SecurityConfig.UserConfig()));
    }

    @Test
    public void getRealmRoles_nominal() {
        PrincipalProvider<String> identity = Principals.identityPrincipal(USERNAME);
        Set<String> foundRoles = underTest.getRealmRoles(identity, Collections.emptyList());
        assertThat(foundRoles).hasSize(2);
        assertThat(foundRoles).contains(ROLE_1);
        assertThat(foundRoles).contains(ROLE_2);
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
        UsernamePasswordToken token = new UsernamePasswordToken(USERNAME, PASSWORD);
        AuthenticationInfo authInfo = underTest.getAuthenticationInfo(token);
        assertThat(authInfo.getIdentityPrincipal().getPrincipal()).isEqualTo(USERNAME);
    }

    @Test(expected = IncorrectCredentialsException.class)
    public void getAuthenticationInfo_throws_exception_if_incorrect_password() {
        UsernamePasswordToken token = new UsernamePasswordToken(USERNAME, "");
        underTest.getAuthenticationInfo(token);
    }

    @Test(expected = UnknownAccountException.class)
    public void getAuthenticationInfo_throws_exception_if_unknown_user() {
        UsernamePasswordToken token = new UsernamePasswordToken("", PASSWORD);
        underTest.getAuthenticationInfo(token);
    }

    @Test(expected = NullPointerException.class)
    public void null_user_is_invalid() {
        new UsernamePasswordToken(null, PASSWORD);
    }

    @Test(expected = NullPointerException.class)
    public void null_password_is_invalid() {
        UsernamePasswordToken token = new UsernamePasswordToken(USERNAME, (String) null);
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
}
