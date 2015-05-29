/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.spi;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation that gives an explicit name to a Scope class. If a scope class doesn't have this annotation, its
 * simple name will be used instead.
 *
 * @author adrien.lauer@mpsa.com
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface SecurityScope {
    /**
     * @return the name of the scope.
     */
    String value();
}
