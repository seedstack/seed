/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal.realms;

import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import org.seedstack.seed.security.AuthenticationException;
import org.seedstack.seed.security.AuthenticationInfo;
import org.seedstack.seed.security.AuthenticationToken;
import org.seedstack.seed.security.IncorrectCredentialsException;
import org.seedstack.seed.security.Realm;
import org.seedstack.seed.security.RoleMapping;
import org.seedstack.seed.security.RolePermissionResolver;
import org.seedstack.seed.security.UnsupportedTokenException;
import org.seedstack.seed.security.X509CertificateToken;
import org.seedstack.seed.security.principals.PrincipalProvider;
import org.seedstack.seed.security.principals.Principals;
import org.seedstack.seed.security.principals.X509CertificatePrincipalProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A realm that is based on an X509Certificate to identify the user and provide his roles. This realm does not actually
 * authenticates the user as this process should be done by Servlet container.
 */
public class X509CertificateRealm implements Realm {

    private static final Logger LOGGER = LoggerFactory.getLogger(X509CertificateRealm.class);
    private static final String UID = "UID";
    private static final String CN = "CN";
    private RoleMapping roleMapping;
    private RolePermissionResolver rolePermissionResolver;

    @Override
    public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (!(token instanceof X509CertificateToken)) {
            throw new UnsupportedTokenException();
        }
        final X509Certificate[] certificates = ((X509CertificateToken) token).getAuthenticatingCertificates();
        if (certificates.length == 0) {
            throw new IncorrectCredentialsException();
        }
        String uid = null;
        String cn = null;
        // we take the first certificate to extract username
        String dn = certificates[0].getSubjectX500Principal().getName(X500Principal.RFC2253);
        LdapName ln;
        try {
            ln = new LdapName(dn);
        } catch (InvalidNameException e) {
            throw new IncorrectCredentialsException("Certificate does not have a valid DN for user", e);
        }
        for (Rdn rdn : ln.getRdns()) {
            if (rdn.getType().equalsIgnoreCase(UID)) {
                uid = rdn.getValue().toString();
            } else if (rdn.getType().equalsIgnoreCase(CN)) {
                cn = rdn.getValue().toString();
            }
        }
        X509CertificatePrincipalProvider x509Pp = new X509CertificatePrincipalProvider(certificates);
        AuthenticationInfo authInfo;
        if (uid == null) {
            authInfo = new AuthenticationInfo(x509Pp, certificates);
        } else {
            authInfo = new AuthenticationInfo(uid, certificates);
            authInfo.getOtherPrincipals().add(x509Pp);
        }
        if (cn != null) {
            authInfo.getOtherPrincipals().add(Principals.fullNamePrincipal(cn));
        }
        return authInfo;
    }

    @Override
    public Set<String> getRealmRoles(PrincipalProvider<?> identityPrincipal,
            Collection<PrincipalProvider<?>> otherPrincipals) {
        Set<String> realmRoles = new HashSet<>();
        Collection<PrincipalProvider<X509Certificate[]>> certificatePrincipals = Principals.getPrincipalsByType(
                otherPrincipals,
                X509Certificate[].class);
        if (certificatePrincipals.isEmpty()) {
            return Collections.emptySet();
        }
        X509Certificate[] certificates = certificatePrincipals.iterator().next().getPrincipal();
        for (X509Certificate certificate : certificates) {
            String dn = certificate.getIssuerX500Principal().getName(X500Principal.RFC2253);
            LdapName ln;
            try {
                ln = new LdapName(dn);
            } catch (InvalidNameException e) {
                LOGGER.error("Certificate issuer does not have valid DN: " + dn, e);
                continue;
            }
            for (Rdn rdn : ln.getRdns()) {
                if (rdn.getType().equalsIgnoreCase(CN)) {
                    realmRoles.add(rdn.getValue().toString());
                    break;
                }
            }
        }
        return realmRoles;
    }

    @Override
    public RoleMapping getRoleMapping() {
        return roleMapping;
    }

    @Inject
    public void setRoleMapping(@Named("X509CertificateRealm-role-mapping") RoleMapping roleMapping) {
        this.roleMapping = roleMapping;
    }

    @Override
    public RolePermissionResolver getRolePermissionResolver() {
        return rolePermissionResolver;
    }

    @Inject
    public void setRolePermissionResolver(
            @Named("X509CertificateRealm-role-permission-resolver") RolePermissionResolver rolePermissionResolver) {
        this.rolePermissionResolver = rolePermissionResolver;
    }

    @Override
    public Class<? extends AuthenticationToken> supportedToken() {
        return X509CertificateToken.class;
    }
}
