/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.spi;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation that gives an explicit name to a Scope class. If a scope class doesn't have this annotation, its
 * simple name will be used instead.
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface SecurityScope {
    /**
     * @return the name of the scope.
     */
    String value();
}
