/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.testing.junit4;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.CreationException;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.testing.Expected;
import org.seedstack.seed.testing.LaunchWith;
import org.seedstack.seed.testing.junit4.fixtures.TestITLauncher;

@RunWith(SeedITRunner.class)
@Expected(CreationException.class)
@LaunchWith(TestITLauncher.class)
public class PerClassExpectedIT {
    @Inject
    private Object object;

    @Test
    public void injectionShouldNotWork() {
        assertThat(object).isNull();
    }
}
