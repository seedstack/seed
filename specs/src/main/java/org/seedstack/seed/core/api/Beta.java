/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.api;

import java.lang.annotation.*;

/**
 * The Beta annotation marks elements that are subject to incompatible changes.
 * <p>
 * It is not related to lower quality or unsafe API. It just means that using them can cost more work during upgrades.
 * </p>
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE })
public @interface Beta {
}
