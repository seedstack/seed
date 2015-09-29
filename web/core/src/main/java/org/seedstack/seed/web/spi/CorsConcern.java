/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.spi;

import io.nuun.kernel.spi.Concern;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Nuun concern for ordering CORS filter above security.
 *
 * @author adrien.lauer@mpsa.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Concern(name="seed-web-cors-concern", priority= Concern.Priority.HIGHEST, order = 10000)
public @interface CorsConcern {
}
