/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal.authorization;

import java.util.Collection;
import java.util.Collections;
import org.seedstack.seed.security.Permission;
import org.seedstack.seed.security.Role;
import org.seedstack.seed.security.RolePermissionResolver;

/**
 * Simple RolePermissionResolver that provides no permission.
 */
public class EmptyRolePermissionResolver implements RolePermissionResolver {

    @Override
    public Collection<Permission> resolvePermissionsInRole(Role role) {
        return Collections.emptyList();
    }

}
