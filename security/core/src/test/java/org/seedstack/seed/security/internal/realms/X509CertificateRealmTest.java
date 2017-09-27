/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal.realms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.security.AuthenticationInfo;
import org.seedstack.seed.security.AuthenticationToken;
import org.seedstack.seed.security.IncorrectCredentialsException;
import org.seedstack.seed.security.RoleMapping;
import org.seedstack.seed.security.RolePermissionResolver;
import org.seedstack.seed.security.UnsupportedTokenException;
import org.seedstack.seed.security.X509CertificateToken;
import org.seedstack.seed.security.principals.PrincipalProvider;
import org.seedstack.seed.security.principals.Principals;
import org.seedstack.seed.security.principals.X509CertificatePrincipalProvider;

public class X509CertificateRealmTest {

    private X509CertificateRealm underTest;

    private X509Certificate x509Certificate;

    private RoleMapping roleMapping;

    private RolePermissionResolver rolePermissionResolver;

    @Before
    public void before() {
        underTest = new X509CertificateRealm();
        x509Certificate = mock(X509Certificate.class);
        roleMapping = mock(RoleMapping.class);
        rolePermissionResolver = mock(RolePermissionResolver.class);
        underTest.setRoleMapping(roleMapping);
        underTest.setRolePermissionResolver(rolePermissionResolver);
    }

    @Test
    public void getters_should_return_attributes() {
        assertThat(underTest.getRoleMapping()).isEqualTo(roleMapping);
        assertThat(underTest.getRolePermissionResolver()).isEqualTo(rolePermissionResolver);
    }

    @Test
    public void getAuthenticationInfo_should_return_authentication_info() {
        String id = "a123456";
        AuthenticationToken token = new X509CertificateToken(new X509Certificate[]{x509Certificate});
        X500Principal x500Principal = new X500Principal("CN=John Doe, OU=SI, O=PSA, UID=" + id + ", C=foo");
        when(x509Certificate.getSubjectX500Principal()).thenReturn(x500Principal);
        AuthenticationInfo authInfo = underTest.getAuthenticationInfo(token);

        assertThat(authInfo.getIdentityPrincipal().getPrincipal()).isEqualTo(id);
        PrincipalProvider<X509Certificate[]> x509pp = Principals.getOnePrincipalByType(authInfo.getOtherPrincipals(),
                X509Certificate[].class);
        assertThat(x509pp.getPrincipal()[0]).isEqualTo(x509Certificate);
    }

    @Test(expected = UnsupportedTokenException.class)
    public void getAuthenticationInfo_should_throw_exception_if_unsupported_token() {
        underTest.getAuthenticationInfo(mock(AuthenticationToken.class));
    }

    @Test(expected = IncorrectCredentialsException.class)
    public void getAuthenticationInfo_should_throw_exception_if_token_empty() {
        X509CertificateToken token = new X509CertificateToken(new X509Certificate[0]);
        underTest.getAuthenticationInfo(token);
    }

    @Test
    public void getAuthenticationInfo_no_uid() {
        AuthenticationToken token = new X509CertificateToken(new X509Certificate[]{x509Certificate});
        X500Principal x500Principal = new X500Principal("CN=John Doe, OU=SI, O=PSA");
        when(x509Certificate.getSubjectX500Principal()).thenReturn(x500Principal);
        AuthenticationInfo authInfo = underTest.getAuthenticationInfo(token);
        X509Certificate[] principal = (X509Certificate[]) authInfo.getIdentityPrincipal().getPrincipal();
        assertThat(principal[0]).isEqualTo(x509Certificate);
    }

    @Test
    public void getRealmRoles_should_return_roles() {
        X509Certificate[] certificates = new X509Certificate[2];
        certificates[0] = x509Certificate;
        String cn1 = "foobar";
        String cn2 = "barfoo";
        X500Principal x500Principal1 = new X500Principal("CN=" + cn1 + ", OU=ou, o=PSA");
        X500Principal x500Principal2 = new X500Principal("CN=" + cn2 + ", OU=ou, o=PSA");
        when(x509Certificate.getIssuerX500Principal()).thenReturn(x500Principal1);

        X509Certificate x509Certificate2 = mock(X509Certificate.class);
        when(x509Certificate2.getIssuerX500Principal()).thenReturn(x500Principal2);
        certificates[1] = x509Certificate2;

        X509CertificatePrincipalProvider x509CertificatePp = new X509CertificatePrincipalProvider(certificates);
        Collection<PrincipalProvider<?>> list = new ArrayList<>();
        list.add(x509CertificatePp);
        Set<String> roles = underTest.getRealmRoles(Principals.identityPrincipal("uid"), list);

        assertThat(roles).containsOnly(cn1, cn2);
    }
}
