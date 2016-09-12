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
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import org.seedstack.seed.LifecycleListener;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class LifecyclePlugin extends AbstractSeedPlugin implements LifecycleListenerScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(LifecyclePlugin.class);
    private final Set<Class<? extends LifecycleListener>> lifecycleListenerClasses = new HashSet<>();

    @Override
    public String name() {
        return "lifecycle";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().subtypeOf(LifecycleListener.class).build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState initialize(InitContext initContext) {
        for (Class<?> candidate : initContext.scannedSubTypesByParentClass().get(LifecycleListener.class)) {
            if (LifecycleListener.class.isAssignableFrom(candidate)) {
                lifecycleListenerClasses.add((Class<? extends LifecycleListener>) candidate);
                LOGGER.trace("Detected lifecycle listener {}", candidate.getCanonicalName());
            }
        }
        LOGGER.debug("Detected {} lifecycle listener(s)", lifecycleListenerClasses.size());
        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                Multibinder<LifecycleListener> lifecycleListenerMultibinder = Multibinder.newSetBinder(binder(), LifecycleListener.class);
                for (Class<? extends LifecycleListener> lifecycleListenerClass : lifecycleListenerClasses) {
                    lifecycleListenerMultibinder.addBinding().to(lifecycleListenerClass);
                }
            }
        };
    }

    @Inject
    private Set<LifecycleListener> lifecycleListeners;

    @Override
    public Collection<LifecycleListener> get() {
        return lifecycleListeners;
    }
}
