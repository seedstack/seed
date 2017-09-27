/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest;

import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.it.SeedITRunner;

@RunWith(SeedITRunner.class)
public class HypermediaIT {

    @Inject
    private RelRegistry relRegistry;

    @Test
    public void relRegistryIsInjectable() {
        Assertions.assertThat(relRegistry).isNotNull();
    }
}
