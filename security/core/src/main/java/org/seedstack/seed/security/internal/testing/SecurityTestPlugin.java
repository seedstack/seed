/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal.testing;

import java.util.ArrayList;
import java.util.List;
import org.seedstack.seed.testing.spi.TestContext;
import org.seedstack.seed.testing.spi.TestDecorator;
import org.seedstack.seed.testing.spi.TestPlugin;

/**
 * IT plugin for security. Handles the WithSecurity annotation.
 */
public class SecurityTestPlugin implements TestPlugin {
    @Override
    public boolean enabled(TestContext testContext) {
        return true;
    }

    @Override
    public List<Class<? extends TestDecorator>> decorators() {
        List<Class<? extends TestDecorator>> decorators = new ArrayList<>();
        decorators.add(WithUserTestDecorator.class);
        return decorators;
    }
}
