/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.realms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import org.seedstack.seed.security.AuthenticationInfo;
import org.seedstack.seed.security.AuthenticationToken;
import org.seedstack.seed.security.UsernamePasswordToken;
import org.seedstack.seed.security.IncorrectCredentialsException;
import org.seedstack.seed.security.UnknownAccountException;
import org.seedstack.seed.security.UnsupportedTokenException;
import org.seedstack.seed.security.principals.PrincipalProvider;
import org.seedstack.seed.security.principals.Principals;
import org.seedstack.seed.security.internal.realms.ConfigurationRealm.ConfigurationUser;

public class ConfigurationRealmUnitTest {

    private ConfigurationRealm underTest;

    private Set<ConfigurationUser> users;

    String username = "username";
    String password = "password";
    String role1 = "role1";
    String role2 = "role2";

    @Before
    @SuppressWarnings("unchecked")
    public void before() {
        underTest = new ConfigurationRealm();
        users = (Set<ConfigurationUser>) Whitebox.getInternalState(underTest, "users");
        ConfigurationUser user = new ConfigurationUser(username);
        Whitebox.setInternalState(user, "password", password);
        user.getRoles().add(role1);
        user.getRoles().add(role2);
        users.add(user);
        users.add(new ConfigurationUser("toto"));
    }

    @Test
    public void getRealmRoles_nominal() {
        PrincipalProvider<String> identity = Principals.identityPrincipal(username);

        Set<String> foundRoles = underTest.getRealmRoles(identity, Collections.<PrincipalProvider<?>>emptyList());

        assertThat(foundRoles).hasSize(2);
        assertThat(foundRoles).contains(role1);
        assertThat(foundRoles).contains(role2);
    }

    @Test
    public void getRealmRoles_returns_empty_if_user_unknown() {
        String username = "titi";
        PrincipalProvider<String> identity = Principals.identityPrincipal(username);

        Set<String> foundRoles = underTest.getRealmRoles(identity, Collections.<PrincipalProvider<?>>emptyList());

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
    public void getAuthenticationInfo_throws_exception_if_null_user() {
        UsernamePasswordToken token = new UsernamePasswordToken(null, password);
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
    public void readConfiguration_empty_props(){
        underTest.readConfiguration(new PropertiesConfiguration());
    }
    
    @Test
    public void readConfiguration_with_users(){
        PropertiesConfiguration conf = new PropertiesConfiguration();
        conf.setProperty("users.Obiwan", "yodarulez, SEED.JEDI");
        conf.setProperty("users.Anakin", "imsodark");
        underTest.readConfiguration(conf);
        
        assertThat(users).hasSize(2);
        assertThat(users).containsOnly(new ConfigurationUser("Obiwan"), new ConfigurationUser("Anakin"));
    }
}
