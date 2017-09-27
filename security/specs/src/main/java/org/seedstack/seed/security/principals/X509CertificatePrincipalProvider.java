/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.principals;

import static java.util.Objects.requireNonNull;

import java.security.cert.X509Certificate;

/**
 * Principal provider that stores the user X509Certificates used for his
 * authentication.
 */
public class X509CertificatePrincipalProvider implements PrincipalProvider<X509Certificate[]> {
    private X509Certificate[] certificates;

    /**
     * Constructor
     *
     * @param x509Certificates the user certificates.
     */
    public X509CertificatePrincipalProvider(X509Certificate[] x509Certificates) {
        this.certificates = requireNonNull(x509Certificates, "X509 certificates array should not be null").clone();
    }

    @Override
    public X509Certificate[] getPrincipal() {
        return certificates.clone();
    }
}
