/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.principals;

import java.io.Serializable;

/**
 * Represents a principal of a user, which is an attribute.
 *
 * @param <T> the type of the object provided by the principal
 */
public interface PrincipalProvider<T> {
    /**
     * For compatibility purposes.
     *
     * @param <X> a serializable type
     * @return the object cast as X.
     * @deprecated
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    default <X extends Serializable> X getPrincipal() {
        return (X) get();
    }

    /**
     * Gives the enclosed principal.
     *
     * @return the object enclosed in the principal
     */
    T get();
}
