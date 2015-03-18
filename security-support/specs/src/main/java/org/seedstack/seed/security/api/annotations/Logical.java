/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.api.annotations;

/**
 * An enum to define AND and OR logical operators to use with annotations RequireRoles or RequiresPermissions
 * @author yves.dautremay@mpsa.com
 *
 */
public enum Logical {

    /** AND */
	AND,
	
	/** OR */
	OR;
}
