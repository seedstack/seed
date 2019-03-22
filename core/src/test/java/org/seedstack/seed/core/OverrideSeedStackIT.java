/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Application;
import org.seedstack.seed.testing.LaunchMode;
import org.seedstack.seed.testing.LaunchWith;
import org.seedstack.seed.testing.SystemProperty;
import org.seedstack.seed.testing.junit4.SeedITRunner;

@RunWith(SeedITRunner.class)
@LaunchWith(mode = LaunchMode.PER_TEST)
public class OverrideSeedStackIT {
    @Inject
    private Application application;

    @Test
    public void normalApplicationIsInjectable() {
        assertThat(application.getName()).isEqualTo("seed-it");
    }

    @Test
    @SystemProperty(name = "additionalPackage", value = "custom")
    public void customApplicationIsInjectable() {
        assertThat(application.getName()).isEqualTo("custom");
    }
}
