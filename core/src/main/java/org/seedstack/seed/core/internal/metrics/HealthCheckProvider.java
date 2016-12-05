/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.metrics;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.seedstack.seed.core.internal.dependency.DependencyClassProxy;
import org.seedstack.seed.spi.dependency.DependencyProvider;

/**
 * Provider used to get a {@link HealthCheckRegistry} to register an internal {@link HealthCheck}.
 */
public class HealthCheckProvider implements DependencyProvider {

    private HealthCheckRegistry healthCheckRegistry;

    @Override
    public String getClassToCheck() {
        return "com.codahale.metrics.health.HealthCheckRegistry";
    }

    public HealthCheckRegistry getHealthCheckRegistry() {
        if (this.healthCheckRegistry == null) {
            this.healthCheckRegistry = new HealthCheckRegistry();
        }
        return healthCheckRegistry;
    }

    /**
     * Register a new {@link HealthCheck}.
     *
     * @param name        healthcheck name to register.
     * @param healthCheck healthcheck to register.
     */
    public void register(String name, HealthCheck healthCheck) {
        getHealthCheckRegistry().register(name, healthCheck);
    }

    /**
     * Register a new {@link HealthCheck} from a proxy.
     *
     * @param name           healthcheck name to register.
     * @param methodReplacer proxy method replacer to override {@link HealthCheck} methods.
     */
    public void register(String name, HealthCheckMethodReplacer methodReplacer) {
        getHealthCheckRegistry().register(name, new DependencyClassProxy<>(HealthCheck.class, methodReplacer).getProxy());
    }

}
