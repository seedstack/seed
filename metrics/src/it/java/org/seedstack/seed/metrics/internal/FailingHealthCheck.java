/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.health.HealthCheck;
import org.seedstack.seed.metrics.HealthChecked;

@HealthChecked
public class FailingHealthCheck extends HealthCheck {
    @Override
    protected Result check() throws Exception {
        return Result.unhealthy("oops");
    }
}
