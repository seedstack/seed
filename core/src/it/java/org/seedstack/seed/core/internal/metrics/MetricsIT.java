/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.metrics;

import static io.nuun.kernel.core.NuunCore.createKernel;
import static io.nuun.kernel.core.NuunCore.newKernelConfiguration;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seedstack.seed.spi.dependency.Maybe;
import org.seedstack.seed.core.utils.DependencyProxy;
import org.seedstack.seed.core.utils.ProxyMethodReplacer;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;

import io.nuun.kernel.api.Kernel;

public class MetricsIT {
    static Kernel underTest;
    private Injector injector;

    public static class MyObjectWithMetrics {

    	private static final long GAUGE_VALUE = 4L;
		public static final String NEW_GAUGE = "new-gauge";
    	public  static final String NEW_METRIC = "new-metric";
    	@Inject
    	Maybe<MetricsProvider> metricsProvider;
    	
    	public void start(){
    		if (metricsProvider.isPresent()) {
    			final Counter c = new Counter();
    			metricsProvider.get().register(NEW_METRIC, new MetricHandler() {
    				
    				@Override
    				public Metric handle() {
    					return c;
    				}
    			});
    			c.inc();
    			
    			DependencyProxy<Gauge<Long>> gauge = new DependencyProxy<Gauge<Long>>(new Class[]{Gauge.class}, new ProxyMethodReplacer() {
    	        	@SuppressWarnings("unused")
    				public Long getValue(){
    	        		return GAUGE_VALUE;
    	        	}
    			});
    			
    	        metricsProvider.get().register(NEW_GAUGE,gauge.getProxy());

    		}
    	}
    }

    @BeforeClass
    public static void beforeClass() {
        underTest = createKernel(newKernelConfiguration());
        underTest.init();
        underTest.start();
    }

    @AfterClass
    public static void afterClass() {
        underTest.stop();
    }

    @Before
    public void before() {

        Module aggregationModule = new AbstractModule() {

            @Override
            protected void configure() {
                bind(MyObjectWithMetrics.class);
            }
        };
        injector = underTest.objectGraph().as(Injector.class).createChildInjector(
                aggregationModule);
    }
    
	/**
	 * Test metrics are added if metrics is in the classpath
	 */
	@Test
	public void test() {
        MyObjectWithMetrics o = injector.getInstance(MyObjectWithMetrics.class);
        o.start();
        Maybe<MetricsProvider> provider = injector.getInstance(Key.get(new TypeLiteral<Maybe<MetricsProvider>>(){}));
        Assertions.assertThat(provider.isPresent()).isTrue();
        MetricRegistry metricRegistry = provider.get().getMetricRegistry();
        Assertions.assertThat(metricRegistry.counter(MyObjectWithMetrics.NEW_METRIC)).isNotNull();
        Counter c = metricRegistry.counter(MyObjectWithMetrics.NEW_METRIC);
        Assertions.assertThat(c.getCount()).isEqualTo(1);
        Assertions.assertThat(metricRegistry.getGauges().containsKey(MyObjectWithMetrics.NEW_GAUGE)).isNotNull();
        @SuppressWarnings("unchecked")
		Gauge<Long> gauge = metricRegistry.getGauges().get(MyObjectWithMetrics.NEW_GAUGE);
        Assertions.assertThat(gauge.getValue()).isEqualTo(MyObjectWithMetrics.GAUGE_VALUE);

	}

}
