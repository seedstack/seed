/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.lifecycle;

import com.google.inject.spi.ProvisionListener;
import org.seedstack.shed.reflect.Classes;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static org.seedstack.shed.reflect.AnnotationPredicates.elementAnnotatedWith;
import static org.seedstack.shed.reflect.ReflectUtils.invoke;
import static org.seedstack.shed.reflect.ReflectUtils.makeAccessible;

class LifecycleProvisionListener implements ProvisionListener {
    private final LifecycleManager lifecycleManager;

    LifecycleProvisionListener(LifecycleManager lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
    }

    @Override
    public <T> void onProvision(ProvisionInvocation<T> provisionInvocation) {
        T provision = provisionInvocation.provision();
        Classes.from(provision.getClass())
                .traversingInterfaces()
                .traversingSuperclasses()
                .methods()
                .forEach(m -> {
                    if (elementAnnotatedWith(PostConstruct.class, true).test(m)) {
                        invoke(makeAccessible(m), provision);
                    }

                    // End-of-life management are limited to singletons (in-line with the matching condition in LifecycleMatcher)
                    if (provisionInvocation.getBinding().acceptScopingVisitor(SingletonScopingVisitor.INSTANCE)) {
                        if (elementAnnotatedWith(PreDestroy.class, true).test(m)) {
                            lifecycleManager.registerPreDestroy(provision, m);
                        }
                        if (provision instanceof AutoCloseable) {
                            lifecycleManager.registerAutoCloseable((AutoCloseable) provision);
                        }
                    }
                });
    }
}
