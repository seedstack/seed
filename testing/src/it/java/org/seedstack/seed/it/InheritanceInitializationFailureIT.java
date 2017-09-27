/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.it;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import org.junit.Test;

@Expect(RuntimeException.class)
public class InheritanceInitializationFailureIT extends AbstractSeedIT {
    @Inject
    private Object object;

    @Test
    public void injection_should_not_work() {
        assertThat(object).isNull();
    }
}
