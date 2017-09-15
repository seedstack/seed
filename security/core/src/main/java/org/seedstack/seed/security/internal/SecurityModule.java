/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.PrivateElements;
import org.apache.commons.lang.ArrayUtils;
import org.apache.shiro.event.EventBus;
import org.apache.shiro.mgt.SecurityManager;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.security.Scope;
import org.seedstack.seed.security.internal.securityexpr.SecurityExpressionModule;
import org.seedstack.seed.security.spi.CrudActionResolver;

import java.util.Collection;
import java.util.Map;

@SecurityConcern
class SecurityModule extends AbstractModule {
    private static final Key<?>[] excludedKeys = new Key<?>[]{
            Key.get(SecurityManager.class),
            Key.get(EventBus.class)
    };
    private final Map<String, Class<? extends Scope>> scopeClasses;
    private final SecurityConfigurer securityConfigurer;
    private final boolean elAvailable;
    private final Collection<SecurityProvider> securityProviders;
    private final Collection<Class<? extends CrudActionResolver>> crudActionResolvers;

    SecurityModule(SecurityConfigurer securityConfigurer, Map<String, Class<? extends Scope>> scopeClasses, boolean elAvailable, Collection<SecurityProvider> securityProviders, Collection<Class<? extends CrudActionResolver>> crudActionResolvers) {
        this.securityConfigurer = securityConfigurer;
        this.scopeClasses = scopeClasses;
        this.elAvailable = elAvailable;
        this.securityProviders = securityProviders;
        this.crudActionResolvers = crudActionResolvers;
    }

    @Override
    protected void configure() {
        install(new SecurityInternalModule(securityConfigurer, scopeClasses));
        install(new SecurityAopModule(crudActionResolvers));

        if (elAvailable) {
            install(new SecurityExpressionModule());
        }

        Module mainModuleToInstall = null;
        for (SecurityProvider securityProvider : securityProviders) {
            Module mainSecurityModule = securityProvider.provideMainSecurityModule(new SecurityGuiceConfigurer(securityConfigurer.getSecurityConfiguration()));
            if (mainSecurityModule != null) {
                if (mainModuleToInstall == null || mainModuleToInstall instanceof DefaultSecurityModule) {
                    mainModuleToInstall = mainSecurityModule;
                } else if (!(mainSecurityModule instanceof DefaultSecurityModule)) {
                    throw SeedException
                            .createNew(SecurityErrorCode.MULTIPLE_MAIN_SECURITY_MODULES)
                            .put("first", mainModuleToInstall.getClass().getCanonicalName())
                            .put("second", mainSecurityModule.getClass().getCanonicalName());
                }
            }

            Module additionalSecurityModule = securityProvider.provideAdditionalSecurityModule();
            if (additionalSecurityModule != null) {
                install(removeSecurityManager(additionalSecurityModule));
            }
        }
        install(mainModuleToInstall);
    }

    private Module removeSecurityManager(Module module) {
        return new ModuleWithoutSecurityManager((PrivateElements) Elements.getElements(module).iterator().next());
    }

    private static class ModuleWithoutSecurityManager extends PrivateModule {
        private final PrivateElements privateElements;

        private ModuleWithoutSecurityManager(PrivateElements privateElements) {
            this.privateElements = privateElements;
        }

        @Override
        protected void configure() {
            for (Element element : privateElements.getElements()) {
                if (element instanceof Binding && ArrayUtils.contains(excludedKeys, ((Binding) element).getKey())) {
                    continue;
                }
                element.applyTo(binder());
            }

            for (Key<?> exposedKey : privateElements.getExposedKeys()) {
                if (ArrayUtils.contains(excludedKeys, exposedKey)) {
                    continue;
                }
                expose(exposedKey);
            }
        }
    }
}
