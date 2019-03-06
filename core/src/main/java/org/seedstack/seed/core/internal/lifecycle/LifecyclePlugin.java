/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.lifecycle;

import static org.seedstack.shed.misc.PriorityUtils.sortByPriority;

import com.google.common.collect.Sets;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.seedstack.seed.Ignore;
import org.seedstack.seed.LifecycleListener;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.seedstack.shed.misc.PriorityUtils;
import org.seedstack.shed.reflect.Annotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifecyclePlugin extends AbstractSeedPlugin implements LifecycleManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(LifecyclePlugin.class);
    private final Set<Class<? extends LifecycleListener>> lifecycleListenerClasses = new HashSet<>();
    private final Set<AutoCloseable> autoCloseableObjects = Sets.newConcurrentHashSet();
    @Inject
    private Set<LifecycleListener> lifecycleListeners;

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
        return new LifecycleModule(lifecycleListenerClasses, this);
    }

    @Override
    public void started() {
        List<? extends LifecycleListener> sortedLifecycleListeners = new ArrayList<>(this.lifecycleListeners);
        sortByPriority(sortedLifecycleListeners, PriorityUtils::priorityOfClassOf);

        for (LifecycleListener lifecycleListener : sortedLifecycleListeners) {
            try {
                LOGGER.info("Executing started method of lifecycle listener {}",
                        lifecycleListener.getClass().getName());
                lifecycleListener.started();
            } catch (Exception e) {
                throw SeedException
                        .wrap(e, CoreErrorCode.ERROR_IN_LIFECYCLE_LISTENER)
                        .put("lifecycleListenerClass", lifecycleListener.getClass().getCanonicalName())
                        .put("phase", "start");
            }
        }
    }

    @Override
    public void stopping() {
        autoCloseableObjects.forEach(closeable -> {
            try {
                LOGGER.info("Closing {}", closeable.getClass().getName());
                closeable.close();
            } catch (Exception e) {
                LOGGER.error("An exception occurred in the close() method of auto-closeable {}",
                        closeable.getClass().getName(), e);
            }
        });

        List<? extends LifecycleListener> sortedLifecycleListeners = new ArrayList<>(this.lifecycleListeners);
        sortByPriority(sortedLifecycleListeners, PriorityUtils::priorityOfClassOf);

        for (LifecycleListener lifecycleListener : sortedLifecycleListeners) {
            try {
                LOGGER.info("Executing stopping method of lifecycle listener {}",
                        lifecycleListener.getClass().getName());
                lifecycleListener.stopping();
            } catch (Exception e) {
                LOGGER.error("An exception occurred in the stopping() method of lifecycle listener {}",
                        lifecycleListener.getClass().getName(), e);
            }
        }
    }

    @Override
    public void registerAutoCloseable(AutoCloseable autoCloseable) {
        try {
            Method closeMethod = autoCloseable.getClass().getMethod("close");
            if (!Annotations.on(closeMethod).traversingOverriddenMembers().find(Ignore.class).isPresent()) {
                if (autoCloseableObjects.add(autoCloseable)) {
                    LOGGER.info("Registered auto-closeable {} for closing at shutdown",
                            autoCloseable.getClass().getName());
                }
            } else {
                LOGGER.debug("Ignored registration of auto-closeable {} for closing at shutdown",
                        autoCloseable.getClass().getName());
            }
        } catch (NoSuchMethodException e) {
            throw SeedException.wrap(e, CoreErrorCode.UNEXPECTED_EXCEPTION);
        }
    }
}
