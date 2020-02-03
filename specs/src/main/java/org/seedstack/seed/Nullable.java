/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The annotated element could be null under some circumstances.
 *
 * <p>
 * Can be used on injection points to specify that injecting a null is allowed.
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Nullable {

}
