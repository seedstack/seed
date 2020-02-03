/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import java.util.Collection;
import java.util.Set;
import org.seedstack.seed.security.principals.PrincipalProvider;

/**
 * Interface used to define a mapping between the data coming from a realm and
 * the roles given to the user. The realm gives all the data so the RoleMapping
 * can apply rules considering them.
 */
public interface RoleMapping {

    /**
     * Resolve the {@link Role}s from the given data.
     * <p>
     * The {@code RoleMapping} can have its own rules whether or not to give a
     * {@code Role}. These rules can be based on the given
     * {@link org.seedstack.seed.security.principals.PrincipalProvider}s.
     * <p>
     * The principalProviders collection is not null but the principal you may
     * look for might not be present.
     *
     * @param realmData          the authorization data coming from the realm as a set of
     *                           String. Not null.
     * @param principalProviders the principalProviders coming from the {@link Realm} of this.
     *                           Not null
     * @return A collection of the {@code Role}s resolved from the data. Not
     * null.
     * @see org.seedstack.seed.security.principals.Principals for utility methods to extract principalProviders
     * from a collection.
     */
    Collection<Role> resolveRoles(Set<String> realmData, Collection<PrincipalProvider<?>> principalProviders);
}
