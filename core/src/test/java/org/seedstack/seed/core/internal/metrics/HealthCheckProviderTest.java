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
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class HealthCheckProviderTest {

	@Test
	public void testGetClassToCheck() {
		HealthCheckProvider healthCheckProvider = new HealthCheckProvider();
		Assertions.assertThat(healthCheckProvider.getClassToCheck()).isNotEmpty();
	}

	@Test
	public void testGetHealthCheckRegistry() {
		HealthCheckProvider healthCheckProvider = new HealthCheckProvider();
		HealthCheckRegistry registry = healthCheckProvider.getHealthCheckRegistry();
		Assertions.assertThat(registry).isNotNull();
		Assertions.assertThat(healthCheckProvider.getHealthCheckRegistry()).isEqualTo(registry);
	}

	@Test
	public void testRegisterStringHealthCheck() {
		HealthCheckProvider healthCheckProvider = new HealthCheckProvider();
		HealthCheck healthCheck = new HealthCheck(){

			@Override
			protected Result check() throws Exception {
				return null;
			}
		};
		final String name = "name";
		healthCheckProvider.register(name, healthCheck);
		Assertions.assertThat(healthCheckProvider.getHealthCheckRegistry().getNames().contains(name)).isTrue();
	}

	@Test
	public void testRegisterStringHealthCheckMethodReplacer() {
		HealthCheckProvider healthCheckProvider = new HealthCheckProvider();
		final String name = "name";
		healthCheckProvider.register(name, () -> {
            // TODO Auto-generated method stub
            return null;
        });
		Assertions.assertThat(healthCheckProvider.getHealthCheckRegistry().getNames().contains(name)).isTrue();
	}

}
