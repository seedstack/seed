/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.CorePlugin;
import org.seedstack.seed.core.internal.metrics.HealthcheckProvider;
import org.seedstack.seed.core.internal.metrics.MetricsProvider;
import org.seedstack.seed.core.spi.dependency.Maybe;
import org.seedstack.seed.metrics.api.MetricsErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;

import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;

/**
 * This plugin provides support for the Metrics monitoring library (https://dropwizard.github.io/metrics/).
 *
 * @author yves.dautremay@mpsa.com
 */
public class MetricsPlugin extends AbstractPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsPlugin.class);

    private MetricRegistry metricRegistry;
    private HealthCheckRegistry healthCheckRegistry ;
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

        Plugin corePlugin = initContext.pluginsRequired().iterator().next();
        if (!(corePlugin instanceof CorePlugin)) {
            throw new PluginException("Missing CorePlugin");
        }

        Maybe<MetricsProvider> metricsProvider = ((CorePlugin) corePlugin).getDependency(MetricsProvider.class);
        if ( ! metricsProvider.isPresent()) {
        	throw SeedException.createNew(MetricsErrorCode.METRICS_REGISTRY_NOT_FOUND);
        }
        metricRegistry = metricsProvider.get().getMetricRegistry();
        
        Maybe<HealthcheckProvider> healthCheckProvider = ((CorePlugin) corePlugin).getDependency(HealthcheckProvider.class);
        if ( ! metricsProvider.isPresent()) {
        	throw SeedException.createNew(MetricsErrorCode.HEALTHCHECK_REGISTRY_NOT_FOUND);
        }
        healthCheckRegistry = healthCheckProvider.get().getHealthCheckRegistry();

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
	public Collection<Class<? extends Plugin>> requiredPlugins() {
		Collection<Class<? extends Plugin>> list = new ArrayList<Class<? extends Plugin>>();
		list.add(CorePlugin.class);
		return list;
	}
    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().subtypeOf(HealthCheck.class).build();
    }

    @Override
    public Object nativeUnitModule() {
        return new MetricsModule(metricRegistry, healthCheckRegistry, healthCheckClasses);
    }

    public HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry;
    }

	public MetricRegistry getMetricRegistry() {
		return metricRegistry;
	}
    
    
}
