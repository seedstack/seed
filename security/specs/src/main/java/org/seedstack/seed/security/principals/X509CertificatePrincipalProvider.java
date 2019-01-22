/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.principals;

import static java.util.Objects.requireNonNull;

import java.security.cert.X509Certificate;

/**
 * Principal provider that stores the subject X509 certificate chain provided during authentication.
 */
public class X509CertificatePrincipalProvider implements PrincipalProvider<X509Certificate[]> {
    private final X509Certificate[] certificates;

    /**
     * Creates a X509CertificatePrincipalProvider.
     *
     * @param x509Certificates the user certificates.
     */
    public X509CertificatePrincipalProvider(X509Certificate[] x509Certificates) {
        this.certificates = requireNonNull(x509Certificates, "X509 certificates array should not be null").clone();
        if (this.certificates.length == 0) {
            throw new IllegalArgumentException("Empty X509 certificate chain");
        }
    }

    @Override
    public X509Certificate[] getPrincipal() {
        return certificates.clone();
    }
}
