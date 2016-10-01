/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.lifecycle;

import io.nuun.kernel.spi.KernelExtension;
import org.seedstack.seed.LifecycleListener;
import org.seedstack.shed.exception.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;

import java.util.Collection;

public class LifecycleExtension implements KernelExtension<LifecycleListenerScanner> {

    @Override
    public void initializing(Collection<LifecycleListenerScanner> extendedPlugins) {

    }

    @Override
    public void initialized(Collection<LifecycleListenerScanner> extendedPlugins) {
    }

    @Override
    public void starting(Collection<LifecycleListenerScanner> extendedPlugins) {

    }

    @Override
    public void started(Collection<LifecycleListenerScanner> extendedPlugins) {
        for (LifecycleListenerScanner extendedPlugin : extendedPlugins) {
            for (LifecycleListener lifecycleListener : extendedPlugin.get()) {
                try {
                    lifecycleListener.started();
                } catch (Exception e) {
                    throw SeedException
                            .wrap(e, CoreErrorCode.ERROR_DURING_LIFECYCLE_CALLBACK)
                            .put("lifecycleListenerClass", lifecycleListener.getClass().getCanonicalName())
                            .put("phase", "start");
                }
            }
        }
    }

    @Override
    public void stopping(Collection<LifecycleListenerScanner> extendedPlugins) {
        for (LifecycleListenerScanner extendedPlugin : extendedPlugins) {
            for (LifecycleListener lifecycleListener : extendedPlugin.get()) {
                try {
                    lifecycleListener.stopping();
                } catch (Exception e) {
                    throw SeedException
                            .wrap(e, CoreErrorCode.ERROR_DURING_LIFECYCLE_CALLBACK)
                            .put("lifecycleListenerClass", lifecycleListener.getClass().getCanonicalName())
                            .put("phase", "stop");

                }
            }
        }
    }

    @Override
    public void stopped(Collection<LifecycleListenerScanner> extendedPlugins) {

    }
}
