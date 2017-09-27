/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.cli;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedIT;

public class WithoutWithCommandLineIT extends AbstractSeedIT {
    @Inject
    private Fixture fixture;

    @Test
    public void test_without_annotation() {
        assertThat(fixture).isNotNull();
    }
}
