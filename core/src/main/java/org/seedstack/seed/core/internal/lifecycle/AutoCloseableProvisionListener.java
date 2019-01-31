/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.lifecycle;

import com.google.inject.spi.ProvisionListener;

class AutoCloseableProvisionListener implements ProvisionListener {
    private final LifecycleManager lifecycleManager;

    AutoCloseableProvisionListener(LifecycleManager lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
    }

    @Override
    public <T> void onProvision(ProvisionInvocation<T> provisionInvocation) {
        T provision = provisionInvocation.provision();
        if (provision instanceof AutoCloseable) {
            lifecycleManager.registerAutoCloseable(((AutoCloseable) provision));
        }
    }
}
