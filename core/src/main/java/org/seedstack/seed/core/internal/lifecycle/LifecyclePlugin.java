/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.lifecycle;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
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
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import org.seedstack.seed.Ignore;
import org.seedstack.seed.LifecycleListener;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.seedstack.shed.misc.PriorityUtils;
import org.seedstack.shed.reflect.Annotations;
import org.seedstack.shed.reflect.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifecyclePlugin extends AbstractSeedPlugin implements LifecycleManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(LifecyclePlugin.class);
    private final Set<Class<? extends LifecycleListener>> lifecycleListenerClasses = new HashSet<>();
    private final Set<DelayedCall> autoCloseableMethods = Sets.newConcurrentHashSet();
    private final Set<DelayedCall> preDestroyMethods = Sets.newConcurrentHashSet();
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
                LOGGER.debug("Lifecycle listener {} detected", candidate.getCanonicalName());
            }
        }
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
                LOGGER.info("Invoking lifecycle method: {}.started()", lifecycleListener.getClass().getName());
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
        // Auto-closeable
        autoCloseableMethods.forEach(DelayedCall::proceed);

        // @PreDestroy
        preDestroyMethods.forEach(DelayedCall::proceed);

        // Lifecycle listeners stopping()
        List<? extends LifecycleListener> sortedLifecycleListeners = new ArrayList<>(this.lifecycleListeners);
        sortByPriority(sortedLifecycleListeners, PriorityUtils::priorityOfClassOf);
        for (LifecycleListener lifecycleListener : sortedLifecycleListeners) {
            try {
                LOGGER.info("Invoking lifecycle method: {}.stopping()", lifecycleListener.getClass().getName());
                lifecycleListener.stopping();
            } catch (Exception e) {
                LOGGER.error("An exception occurred in lifecycle method: {}.stopping()",
                        lifecycleListener.getClass().getName(), e);
            }
        }
    }

    @Override
    public void registerPreDestroy(Object o, Method m) {
        DelayedCall delayedCall = new DelayedCall(o, m);
        if (delayedCall.shouldBeIgnored()) {
            LOGGER.debug("Ignored registration of @PreDestroy method: {}", delayedCall);
        } else {
            if (preDestroyMethods.add(delayedCall)) {
                LOGGER.debug("@PreDestroy method registered for closing at shutdown: {}", delayedCall);
            }
        }
    }

    @Override
    public void registerAutoCloseable(AutoCloseable autoCloseable) {
        try {
            DelayedCall delayedCall = new DelayedCall(autoCloseable, autoCloseable.getClass().getMethod("close"));
            if (delayedCall.shouldBeIgnored()) {
                LOGGER.debug("Ignored registration of auto-closeable {}", delayedCall);
            } else {
                if (autoCloseableMethods.add(delayedCall)) {
                    LOGGER.debug("Close method registered for closing at shutdown: {}", delayedCall);
                }
            }
        } catch (NoSuchMethodException e) {
            throw SeedException.wrap(e, CoreErrorCode.UNEXPECTED_EXCEPTION);
        }
    }

    private static class DelayedCall {
        private final Object o;
        private final Method m;

        private DelayedCall(Object o, Method m) {
            checkState(m.getDeclaringClass().isAssignableFrom(o.getClass()));
            this.o = checkNotNull(o);
            this.m = checkNotNull(m);
        }

        private boolean shouldBeIgnored() {
            return Annotations.on(m)
                    .traversingOverriddenMembers()
                    .find(Ignore.class)
                    .isPresent();
        }

        private void proceed() {
            try {
                LOGGER.info("Invoking lifecycle method: {}", this);
                ReflectUtils.invoke(ReflectUtils.makeAccessible(m), o);
            } catch (Exception e) {
                LOGGER.error("An exception occurred calling lifecycle method: {}", this, e);
            }
        }

        @Override
        public boolean equals(Object o1) {
            if (this == o1) return true;
            if (o1 == null || getClass() != o1.getClass()) return false;
            DelayedCall that = (DelayedCall) o1;
            return o.equals(that.o) &&
                    m.equals(that.m);
        }

        @Override
        public int hashCode() {
            return Objects.hash(o, m);
        }

        @Override
        public String toString() {
            return String.format("%s.%s()", m.getDeclaringClass().getName(), m.getName());
        }
    }
}
