/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.lifecycle;

import io.nuun.kernel.spi.KernelExtension;
import java.util.Collection;

public class LifecycleExtension implements KernelExtension<LifecycleManager> {
    @Override
    public void initializing(Collection<LifecycleManager> lifecycleManagers) {
        // this phase is not supported by LifecycleManager
    }

    @Override
    public void initialized(Collection<LifecycleManager> lifecycleManagers) {
        // this phase is not supported by LifecycleManager
    }

    @Override
    public void starting(Collection<LifecycleManager> lifecycleManagers) {
        // this phase is not supported by LifecycleManager
    }

    @Override
    public void started(Collection<LifecycleManager> lifecycleManagers) {
        for (LifecycleManager lifecycleManager : lifecycleManagers) {
            lifecycleManager.started();
        }
    }

    @Override
    public void stopping(Collection<LifecycleManager> lifecycleManagers) {
        for (LifecycleManager lifecycleManager : lifecycleManagers) {
            lifecycleManager.stopping();
        }
    }

    @Override
    public void stopped(Collection<LifecycleManager> lifecycleManagers) {
        // this phase is not supported by LifecycleManager
    }
}
