/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.spi;


import io.nuun.kernel.spi.Concern;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Nuun concern for ordering security operations.
 *
 * @author yves.dautremay@mpsa.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Concern(name="seed-security-concern", priority= Concern.Priority.HIGHEST)
public @interface SecurityConcern {

}
