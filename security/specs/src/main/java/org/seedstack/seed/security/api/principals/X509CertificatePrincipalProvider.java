/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.api.principals;

import java.io.Serializable;
import java.security.cert.X509Certificate;

/**
 * Principal provider that stores the user X509Certificates used for his
 * authentication.
 *
 * @author yves.dautremay@mpsa.com
 */
public class X509CertificatePrincipalProvider implements PrincipalProvider<X509Certificate[]>, Serializable {

    private static final long serialVersionUID = -437306763922586396L;

    private X509Certificate[] certificates;

    /**
     * Constructor
     *
     * @param x509Certificates the user certificates.
     */
    public X509CertificatePrincipalProvider(X509Certificate[] x509Certificates) {
        if (x509Certificates == null) {
            this.certificates = null;
        } else {
            this.certificates = x509Certificates.clone();
        }
    }

    @Override
    public X509Certificate[] getPrincipal() {
        if (certificates == null) {
            return null;
        }

        return certificates.clone();
    }
}
