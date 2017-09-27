/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
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
     * Constructor
     *
     * @param x509Certificates the certificates
     */
    public X509CertificateToken(X509Certificate[] x509Certificates) {
        this.certificates = requireNonNull(x509Certificates, "X509 certificates array should not be null").clone();
    }

    /**
     * Gives the certificates that were validated by the server
     *
     * @return the certificates.
     */
    public X509Certificate[] getAuthenticatingCertificates() {
        return certificates.clone();
    }

    @Override
    public Object getPrincipal() {
        return getAuthenticatingCertificates();
    }

    @Override
    public Object getCredentials() {
        return getAuthenticatingCertificates();
    }
}
