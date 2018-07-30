/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.principals;

import javax.security.auth.x500.X500Principal;

/**
 * Principal provider that stores the subject X500Principal provided during authentication.
 */
public class X500PrincipalProvider implements PrincipalProvider<X500Principal> {
    private final X500Principal x500Principal;

    /**
     * Creates a X500PrincipalProvider.
     *
     * @param x500Principal the subject X500Principal.
     */
    public X500PrincipalProvider(X500Principal x500Principal) {
        this.x500Principal = x500Principal;
    }

    @Override
    public X500Principal getPrincipal() {
        return x500Principal;
    }
}
