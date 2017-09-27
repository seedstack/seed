/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.seedstack.seed.core.rules.SeedITRule;

public class IgnoreIT {
    @Rule
    public SeedITRule rule = new SeedITRule(this);
    @Inject
    private Injector injector;

    @Test
    public void testScanWorks() throws Exception {
        ScannedClass instance = injector.getInstance(ScannedClass.class);
        Assertions.assertThat(instance).isNotNull();
    }

    @Test(expected = ConfigurationException.class)
    public void testIgnoreFeature() throws Exception {
        injector.getInstance(IgnoredClass.class);
    }

    public @interface Scan {
    }

    @org.seedstack.seed.Ignore
    @Scan
    static class IgnoredClass {
    }

    @Scan
    static class ScannedClass {
    }
}
