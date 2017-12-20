/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.security.cert.X509Certificate;
import org.junit.Test;

public class X509CertificateTokenTest {

    @Test
    public void testToken() {
        X509Certificate certificate = mock(X509Certificate.class);
        X509CertificateToken token = new X509CertificateToken(new X509Certificate[]{certificate});
        assertThat(token.getAuthenticatingCertificates()).containsExactly(certificate);
        assertThat((X509Certificate[]) token.getCredentials()).containsExactly(certificate);
        assertThat((X509Certificate[]) token.getPrincipal()).containsExactly(certificate);
    }
}
