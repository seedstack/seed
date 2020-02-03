/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import java.io.Serializable;

/**
 * A scope is an additional verification to be made on a permission. When
 * verifying a permission, we verify if the scope given to the user includes the
 * scope to verify.
 *
 * As an example, one could consider hierarchical scopes where EU includes
 * EU/France. If the user has the scope EU on a permission and we verify a
 * permission valid in EU/France, it will be granted. On the contrary if the user
 * has the scope US and we verify a permission valid in EU/France is will not be
 * granted.
 *
 * Often there is no need for hierarchical scopes and the provided {@link SimpleScope}
 * can be used directly. Simple scopes are not hierarchical and are just checked
 * with strict equality.
 */
public interface Scope extends Serializable {

    /**
     * Return the name of the scope.
     *
     * @return the name of the scope.
     */
    String getName();

    /**
     * Returns the value of the scope represented as a string.
     *
     * @return a string representation of the scope.
     */
    String getValue();

    /**
     * Verifies if the current scope includes the given scope.<br>
     *
     * @param scope the scope to check
     * @return whether this includes the given scope
     */
    boolean includes(Scope scope);
}
