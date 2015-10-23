/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.collect.Sets;
import org.seedstack.seed.it.AbstractSeedIT;
import org.junit.Test;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthCheckIT extends AbstractSeedIT {
    @Inject
    HealthCheckRegistry healthCheckRegistry;

    @Test
    public void health_checks_are_correctly_registered() throws Exception {
        assertThat(healthCheckRegistry.runHealthChecks().keySet()).isEqualTo(Sets.newHashSet(
                "org.seedstack.seed.metrics.internal.FailingHealthCheck",
                "org.seedstack.seed.metrics.internal.SuccessfulHealthCheck",
                "org.seedstack.seed.metrics.internal.InjectedHealthCheck"
        ));
    }

    @Test
    public void health_checks_are_injected() throws Exception {
        assertThat(healthCheckRegistry.runHealthCheck("org.seedstack.seed.metrics.internal.InjectedHealthCheck").isHealthy()).isEqualTo(true);
    }
}
