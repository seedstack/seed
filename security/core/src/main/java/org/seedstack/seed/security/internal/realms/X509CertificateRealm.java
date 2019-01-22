/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.realms;

import com.google.common.base.Strings;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
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
import org.seedstack.seed.security.principals.X500PrincipalProvider;
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
    private final RoleMapping roleMapping;
    private final RolePermissionResolver rolePermissionResolver;

    @Inject
    protected X509CertificateRealm(@Named("X509CertificateRealm-role-mapping") RoleMapping roleMapping,
            @Named("X509CertificateRealm-role-permission-resolver") RolePermissionResolver rolePermissionResolver) {
        this.roleMapping = roleMapping;
        this.rolePermissionResolver = rolePermissionResolver;
    }

    @Override
    public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (!(token instanceof X509CertificateToken)) {
            throw new UnsupportedTokenException();
        }

        X500Principal identityPrincipal = (X500Principal) token.getPrincipal();

        // Extract UID and CN from the DN
        String uid = null;
        String cn = null;
        LdapName ln;
        try {
            ln = new LdapName(identityPrincipal.getName(X500Principal.RFC2253));
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

        // Check certificate validity
        X509Certificate subjectX509Certificate = (X509Certificate) token.getCredentials();
        try {
            subjectX509Certificate.checkValidity();
        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
            throw new IncorrectCredentialsException("Subject X509 certificate is not valid", e);
        }

        AuthenticationInfo authInfo;
        if (!Strings.isNullOrEmpty(uid)) {
            // If an uid is available, it is used as the subject identity principal
            authInfo = new AuthenticationInfo(uid, subjectX509Certificate);
            // The X500Principal is available as a separate principal
            authInfo.getOtherPrincipals().add(new X500PrincipalProvider(identityPrincipal));
        } else {
            // If no uid is available, the X500Principal is used as the subject identity principal
            authInfo = new AuthenticationInfo(new X500PrincipalProvider(identityPrincipal), subjectX509Certificate);
        }

        // If the subject full name is available, make it a well-known principal
        if (cn != null) {
            authInfo.getOtherPrincipals().add(Principals.fullNamePrincipal(cn));
        }

        // Make the full certificate chain available as an additional principal
        authInfo.getOtherPrincipals().add(
                new X509CertificatePrincipalProvider(((X509CertificateToken) token).getAuthenticatingCertificates())
        );

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

    @Override
    public RolePermissionResolver getRolePermissionResolver() {
        return rolePermissionResolver;
    }

    @Override
    public Class<? extends AuthenticationToken> supportedToken() {
        return X509CertificateToken.class;
    }
}
