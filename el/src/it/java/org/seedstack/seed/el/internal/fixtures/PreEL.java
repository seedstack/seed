/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.el.internal.fixtures;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * EL executed before the annotated method.
 *
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 01/07/2014
 */
@Target({ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PreEL {

    /**
     * @return expression language
     */
    String value();
}
