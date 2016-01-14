/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedIT;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.collect.Sets;

public class HealthCheckIT extends AbstractSeedIT {
    @Inject
    HealthCheckRegistry healthCheckRegistry;

    @Test
    public void health_checks_are_correctly_registered() throws Exception {
        assertThat(healthCheckRegistry.runHealthChecks().keySet()).isEqualTo(Sets.newHashSet(
                "org.seedstack.seed.metrics.internal.FailingHealthCheck",
                "SuccessfulHealthCheck",
                "org.seedstack.seed.metrics.internal.InjectedHealthCheck"
        ));
    }

    @Test
    public void health_checks_are_injected() throws Exception {
        assertThat(healthCheckRegistry.runHealthCheck("org.seedstack.seed.metrics.internal.InjectedHealthCheck").isHealthy()).isEqualTo(true);
    }
}
