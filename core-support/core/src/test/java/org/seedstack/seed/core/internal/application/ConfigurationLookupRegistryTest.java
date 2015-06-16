/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 16 juin 2015
 */
package org.seedstack.seed.core.internal.application;

import mockit.Mocked;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Unit test for {@link ConfigurationLookupRegistry}
 * 
 * @author thierry.bouvet@mpsa.com
 */
public class ConfigurationLookupRegistryTest {

    @Test
    public void testRegister(@Mocked final ConfigurationLookup lookup) {
        final String key = "lookupKey";
        ConfigurationLookupRegistry.getInstance().register(key, lookup);

        Assertions.assertThat(ConfigurationLookupRegistry.getInstance().getLookups().containsKey(key)).isTrue();
        Assertions.assertThat(ConfigurationLookupRegistry.getInstance().getLookups().get(key)).isEqualTo(lookup);

    }

}
