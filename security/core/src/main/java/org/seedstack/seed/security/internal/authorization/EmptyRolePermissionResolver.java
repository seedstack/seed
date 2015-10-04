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

import org.seedstack.seed.security.api.Permission;
import org.seedstack.seed.security.api.Role;
import org.seedstack.seed.security.api.RolePermissionResolver;

import java.util.Collection;
import java.util.Collections;

/**
 * Simple RolePermissionResolver that provides no permission.
 * 
 * @author yves.dautremay@mpsa.com
 * 
 */
public class EmptyRolePermissionResolver implements RolePermissionResolver {

	@Override
	public Collection<Permission> resolvePermissionsInRole(Role role) {
		return Collections.<Permission> emptyList();
	}

}
