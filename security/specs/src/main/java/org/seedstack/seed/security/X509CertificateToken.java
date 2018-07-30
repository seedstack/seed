/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security;

import static java.util.Objects.requireNonNull;

import java.security.cert.X509Certificate;

/**
 * An authentication token based on certificates.
 */
public class X509CertificateToken implements AuthenticationToken {
    private static final long serialVersionUID = -4213910900796170384L;
    private final X509Certificate[] certificates;

    /**
     * Creates an X509 based authentication token.
     *
     * @param x509Certificates the certificate chain.
     */
    public X509CertificateToken(X509Certificate[] x509Certificates) {
        this.certificates = requireNonNull(x509Certificates, "X509 certificate chain should not be null").clone();
        if (this.certificates.length == 0) {
            throw new IllegalArgumentException("Empty X509 certificate chain");
        }
    }

    /**
     * Returns the certificate chain
     *
     * @return the certificate chain.
     */
    public X509Certificate[] getAuthenticatingCertificates() {
        return certificates.clone();
    }

    /**
     * Returns the subject identity principal.
     *
     * @return the subject {@link javax.security.auth.x500.X500Principal} of the first certificate in the chain.
     */
    @Override
    public Object getPrincipal() {
        // First certificate in the chain is the subject certificate
        return certificates[0].getSubjectX500Principal();
    }

    /**
     * Returns the subject credentials.
     *
     * @return the first certificate in the chain, acting as credentials.
     */
    @Override
    public Object getCredentials() {
        // The subject certificate acts as the credentials
        return certificates[0];
    }
}
