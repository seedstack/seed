/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal.authorization;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.seedstack.seed.security.Role;
import org.seedstack.seed.security.RoleMapping;
import org.seedstack.seed.security.principals.PrincipalProvider;

/**
 * RoleMapping that returns Roles which names are the realm data;
 */
public class SameRoleMapping implements RoleMapping {

    @Override
    public Set<Role> resolveRoles(Set<String> realmData, Collection<PrincipalProvider<?>> principalProviders) {
        Set<Role> roles = new HashSet<>();
        for (String realmDatum : realmData) {
            Role role = new Role(realmDatum);
            roles.add(role);
        }
        return roles;
    }
}
