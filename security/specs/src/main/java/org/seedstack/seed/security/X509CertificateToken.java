/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import java.security.cert.X509Certificate;

/**
 * An authentication token based on certificates.
 *
 * @author yves.dautremay@mpsa.com
 */
public class X509CertificateToken implements AuthenticationToken {

    /**
     * UID
     */
    private static final long serialVersionUID = -4213910900796170384L;

    private final X509Certificate[] certificates;

    /**
     * Constructor
     *
     * @param x509Certificates the certificates
     */
    public X509CertificateToken(X509Certificate[] x509Certificates) {
        if (x509Certificates == null) {
            this.certificates = null;
        } else {
            this.certificates = x509Certificates.clone();
        }
    }

    /**
     * Gives the certificates that were validated by the server
     *
     * @return the certificates.
     */
    public X509Certificate[] getAuthenticatingCertificates() {
        if (certificates == null) {
            return null;
        }

        return certificates.clone();
    }

    @Override
    public Object getPrincipal() {
        return certificates;
    }

    @Override
    public Object getCredentials() {
        return certificates;
    }

}
