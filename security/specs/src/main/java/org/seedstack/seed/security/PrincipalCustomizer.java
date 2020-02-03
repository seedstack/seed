/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import java.util.Collection;
import org.seedstack.seed.security.principals.PrincipalProvider;

/**
 * Interface used to add principals to the one added by a realm
 *
 * @param <R> the Realm type concerned
 */
public interface PrincipalCustomizer<R extends Realm> {

    /**
     * Specifies the realm class to apply
     *
     * @return The Class of the realm
     */
    Class<R> supportedRealm();

    /**
     * Specifies the principals to add to the ones given by the realm. The
     * principals provided as parameters are read only.
     *
     * @param identity        the identity principal.
     * @param realmPrincipals the principals already given by the realm. Read only
     * @return the principals to add.
     */
    Collection<PrincipalProvider<?>> principalsToAdd(PrincipalProvider<?> identity,
            Collection<PrincipalProvider<?>> realmPrincipals);
}
