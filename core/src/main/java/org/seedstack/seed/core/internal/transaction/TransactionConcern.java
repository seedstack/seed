/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.transaction;

import io.nuun.kernel.spi.Concern;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Concern(name = "transaction", priority = Concern.Priority.NORMAL)
@Retention(RetentionPolicy.RUNTIME)
@interface TransactionConcern {
}
