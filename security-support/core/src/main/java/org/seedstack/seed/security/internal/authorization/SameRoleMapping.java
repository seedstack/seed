/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.authorization;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.seedstack.seed.security.api.Role;
import org.seedstack.seed.security.api.RoleMapping;
import org.seedstack.seed.security.api.principals.PrincipalProvider;

/**
 * RoleMapping that returns Roles which names are the realm data;
 * 
 * @author yves.dautremay@mpsa.com
 * 
 */
public class SameRoleMapping implements RoleMapping {

	@Override
	public Set<Role> resolveRoles(Set<String> realmData, Collection<PrincipalProvider<?>> principalProviders) {
		Set<Role> roles = new HashSet<Role>();
		for (String realmDatum : realmData) {
			Role role = new Role(realmDatum);
			roles.add(role);
		}
		return roles;
	}
}
