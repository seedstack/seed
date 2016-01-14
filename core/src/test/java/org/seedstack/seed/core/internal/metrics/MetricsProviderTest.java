/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.metrics;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;

public class MetricsProviderTest {


	@Test
	public void testGetMetricRegistry() {
		MetricsProvider metricsProvider = new MetricsProvider();
		MetricRegistry metricRegistry = metricsProvider.getMetricRegistry();
		Assertions.assertThat(metricRegistry).isNotNull();
		Assertions.assertThat(metricsProvider.getMetricRegistry()).isEqualTo(metricRegistry);
	}

	@Test
	public void testAddMetricStringMetricHandler() {
		final String dummyMetric = "dummyMetric";
		final Counter c = new Counter();
		MetricsProvider metricsProvider = new MetricsProvider();
		metricsProvider.register(dummyMetric, new MetricHandler() {
			@Override
			public Metric handle() {
				return c;
			}
		});
		Assertions.assertThat(metricsProvider.getMetricRegistry().counter(dummyMetric)).isEqualTo(c);
	}

	@Test
	public void testAddMetricStringMetric() {
		final String dummyMetric = "dummyMetric";
		Counter c = new Counter();
		MetricsProvider metricsProvider = new MetricsProvider();
		metricsProvider.register(dummyMetric, c);
		Assertions.assertThat(metricsProvider.getMetricRegistry().counter(dummyMetric)).isEqualTo(c);
	}

	@Test
	public void testGetClassToCheck() {
		MetricsProvider metricsProvider = new MetricsProvider();
		Assertions.assertThat(metricsProvider.getClassToCheck()).isNotEmpty();
	}

}
