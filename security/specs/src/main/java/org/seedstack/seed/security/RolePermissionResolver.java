/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security;

import java.util.Collection;

/**
 * A RolePermissionResolver resolves a {@link Role} and converts it into a
 * {@code Collection} of {@link Permission} instances.
 */
public interface RolePermissionResolver {

    /**
     * Resolves a {@code Collection} of {@link Permission}s based on the given
     * {@link Role}.
     *
     * @param role the {@code Role} to resolve the permissions. Not null.
     * @return a {@code Collection} of {@code Permission}s based on the given
     * {@code Role}. Not null.
     */
    Collection<Permission> resolvePermissionsInRole(Role role);
}
