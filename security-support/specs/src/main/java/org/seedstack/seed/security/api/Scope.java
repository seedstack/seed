/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.api;

/**
 * A scope is an additional verification to be made on a permission. When
 * verifying a permission, we verify is the scope given to the user includes the
 * scope to verify. Scopes can be used as LDAP domains.<br>
 * As an example, one could consider hierarchical scopes where MU includes
 * MU/Montage. If the user has the domain MU on a permission and we verify the
 * permission on MU/Montage, it will be granted.
 *
 * 
 * @author yves.dautremay@mpsa.com
 * 
 */
public interface Scope {

	/**
	 * Technical description of the scope
	 * 
	 * @return a string describing the scope.
	 */
	String getDescription();

	/**
	 * Verifies if the current scope includes the given scope.<br>
	 * 
	 * @param scope the scope to check
	 * @return whether this includes the given scope
	 */
	boolean includes(Scope scope);
}
