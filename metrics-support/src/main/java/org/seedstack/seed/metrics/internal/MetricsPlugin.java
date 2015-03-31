/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This plugin provides support for the Metrics monitoring library (https://dropwizard.github.io/metrics/).
 *
 * @author yves.dautremay@mpsa.com
 */
public class MetricsPlugin extends AbstractPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsPlugin.class);

    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
    private final Set<Class<? extends HealthCheck>> healthCheckClasses = new HashSet<Class<? extends HealthCheck>>();

    @Inject
    private Map<String, HealthCheck> healthChecks;

    @Override
    public String name() {
        return "seed-metrics-plugin";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InitState init(InitContext initContext) {
        Map<Class<?>, Collection<Class<?>>> scannedSubTypesByParentClass = initContext.scannedSubTypesByParentClass();

        for (Class<?> candidate : scannedSubTypesByParentClass.get(HealthCheck.class)) {
            if (HealthCheck.class.isAssignableFrom(candidate)) {
                healthCheckClasses.add((Class<? extends HealthCheck>) candidate);
                LOGGER.trace("detected health check class {}", candidate.getCanonicalName());
            }
        }

        LOGGER.debug("detected {} health check class(es)", healthCheckClasses.size());

        return InitState.INITIALIZED;
    }

    @Override
    public void start(Context context) {
        for (Map.Entry<String, HealthCheck> healthCheckEntry : healthChecks.entrySet()) {
            healthCheckRegistry.register(healthCheckEntry.getKey(), healthCheckEntry.getValue());
        }
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().subtypeOf(HealthCheck.class).build();
    }

    @Override
    public Object nativeUnitModule() {
        return new MetricsModule(metricRegistry, healthCheckRegistry, healthCheckClasses);
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry;
    }
}
