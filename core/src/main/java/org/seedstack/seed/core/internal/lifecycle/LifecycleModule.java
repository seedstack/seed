/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.lifecycle;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.seedstack.seed.LifecycleListener;

import javax.inject.Singleton;
import java.util.Set;

class LifecycleModule extends AbstractModule {
    private final Set<Class<? extends LifecycleListener>> lifecycleListenerClasses;
    private final LifecycleManager lifecycleManager;

    LifecycleModule(Set<Class<? extends LifecycleListener>> lifecycleListenerClasses, LifecycleManager lifecycleManager) {
        this.lifecycleListenerClasses = lifecycleListenerClasses;
        this.lifecycleManager = lifecycleManager;
    }

    @Override
    protected void configure() {
        // Bind LifecycleListeners
        Multibinder<LifecycleListener> lifecycleListenerMultibinder = Multibinder.newSetBinder(binder(), LifecycleListener.class);
        for (Class<? extends LifecycleListener> lifecycleListenerClass : lifecycleListenerClasses) {
            lifecycleListenerMultibinder.addBinding().to(lifecycleListenerClass).in(Singleton.class);
        }

        // Listen for singletons implementing Closeable
        bindListener(new AutoCloseableMatcher(), new AutoCloseableProvisionListener(lifecycleManager));
    }

}
