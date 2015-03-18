/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cache;

import org.seedstack.seed.it.AbstractSeedIT;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

import javax.cache.configuration.CompleteConfiguration;
import javax.inject.Inject;

@Ignore("ignored till we build with java7")
public class CacheIT extends AbstractSeedIT {
    @Inject
    CacheSample cacheSample;

    @Test
    @Ignore("ignored till we build with java7")
    public void method_result_is_effectively_cached() {
        Assertions.assertThat(cacheSample.add1(1, 1)).isEqualTo(2);
        Assertions.assertThat(cacheSample.add1(1, 1)).isEqualTo(2);
    }

    @Test
    @Ignore("ignored till we build with java7")
    public void cache_object_is_injectable() {
        Assertions.assertThat(cacheSample.getCache1()).isNotNull();
        Assertions.assertThat(cacheSample.getCache2()).isNotNull();
    }

    @Test
    @Ignore("ignored till we build with java7")
    public void expiry_factory_configuration_is_honored() {
        Assertions.assertThat(((CompleteConfiguration) cacheSample.getCache1().getConfiguration(CompleteConfiguration.class)).getExpiryPolicyFactory().getClass()).isSameAs(SampleExpiryPolicyFactory.class);
    }
}
