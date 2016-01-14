/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.transaction.spi;

import io.nuun.kernel.spi.Concern;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;





/**
 * Nuun concern for ordering transaction operations.
 *
 * @author adrien.lauer@mpsa.com
 */
@Concern(name = "seed-transaction-concern" , priority= Concern.Priority.NORMAL)
@Retention(RetentionPolicy.RUNTIME)
public @interface TransactionConcern {

}
