/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.BindingRequest;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequestBuilder;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.el.internal.ELPlugin;
import org.seedstack.seed.security.api.*;
import org.seedstack.seed.security.internal.configure.SeedSecurityConfigurer;
import org.seedstack.seed.security.spi.SecurityErrorCodes;
import org.seedstack.seed.security.spi.SecurityScope;
import org.seedstack.seed.security.spi.data.DataObfuscationHandler;
import org.seedstack.seed.security.spi.data.DataSecurityHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This plugin provides core security infrastructure, based on Apache Shiro
 * implementation.
 *
 * @author yves.dautremay@mpsa.com
 * @author adrien.lauer@mpsa.com
 */
public class SecurityPlugin extends AbstractPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityPlugin.class);

    public static final String SECURITY_PREFIX = "org.seedstack.seed.security";

    private final Collection<SecurityProvider> securityProviders = new ArrayList<SecurityProvider>();
    private final Map<String, Class<? extends Scope>> scopeClasses = new HashMap<String, Class<? extends Scope>>();

    private Configuration securityConfiguration;
    private Map<Class<?>, Collection<Class<?>>> scannedClasses;
    private Collection<Class<? extends DataSecurityHandler<?>>> dataSecurityHandlers;
    private Collection<Class<? extends PrincipalCustomizer<?>>> principalCustomizerClasses;
    private boolean elDisabled;

    public SecurityPlugin() {
        for (SecurityProvider securityProvider : ServiceLoader.load(SecurityProvider.class, SeedReflectionUtils.findMostCompleteClassLoader(SecurityPlugin.class))) {
            securityProviders.add(securityProvider);
        }
    }

    @Override
    public String name() {
        return "seed-security-plugin";
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public InitState init(InitContext initContext) {
        for (Plugin plugin : initContext.pluginsRequired()) {
            if (plugin instanceof ApplicationPlugin) {
                securityConfiguration = ((ApplicationPlugin) plugin).getApplication().getConfiguration().subset(SECURITY_PREFIX);
            } else if (plugin instanceof ELPlugin) {
                elDisabled = ((ELPlugin) plugin).isDisabled();
            }
        }

        scannedClasses = initContext.scannedSubTypesByAncestorClass();
        principalCustomizerClasses = (Collection) scannedClasses.get(PrincipalCustomizer.class);
        dataSecurityHandlers = (Collection) scannedClasses.get(DataSecurityHandler.class);

        Collection<Class<? extends Scope>> scopeCandidateClasses = (Collection) scannedClasses.get(Scope.class);
        if (scopeCandidateClasses != null) {
            for (Class<?> scopeCandidateClass : scopeCandidateClasses) {
                if (Scope.class.isAssignableFrom(scopeCandidateClass)) {
                    SecurityScope securityScope = scopeCandidateClass.getAnnotation(SecurityScope.class);
                    String scopeName;

                    if (securityScope != null) {
                        scopeName = securityScope.value();
                    } else {
                        scopeName = scopeCandidateClass.getSimpleName();
                    }

                    try {
                        scopeCandidateClass.getConstructor(String.class);
                    } catch (NoSuchMethodException e) {
                        throw SeedException.wrap(e, SecurityErrorCodes.MISSING_ADEQUATE_SCOPE_CONSTRUCTOR).put("scopeName", scopeName);
                    }

                    if (scopeClasses.containsKey(scopeName)) {
                        throw SeedException.createNew(SecurityErrorCodes.DUPLICATE_SCOPE_NAME).put("scopeName", scopeName);
                    }

                    scopeClasses.put(scopeName, (Class<? extends Scope>) scopeCandidateClass);
                }
            }
        }

        for (SecurityProvider securityProvider : securityProviders) {
            LOGGER.debug("Initializing security provider {}", securityProvider.getClass().getCanonicalName());
            securityProvider.init(initContext);
        }

        if (elDisabled) {
            LOGGER.warn("No Java EL support, data security is disabled");
        }

        return InitState.INITIALIZED;
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        for (SecurityProvider securityProvider : securityProviders) {
            securityProvider.provideContainerContext(containerContext);
        }
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        plugins.add(ELPlugin.class);
        return plugins;
    }

    @Override
    public Object nativeUnitModule() {
        return new SecurityModule(
                new SeedSecurityConfigurer(securityConfiguration, scannedClasses, principalCustomizerClasses),
                scopeClasses,
                dataSecurityHandlers,
                elDisabled,
                securityProviders
        );
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        // Core plugin requests
        ClasspathScanRequestBuilder builder = classpathScanRequestBuilder()
                .descendentTypeOf(Realm.class)
                .descendentTypeOf(RoleMapping.class)
                .descendentTypeOf(RolePermissionResolver.class)
                .descendentTypeOf(Scope.class)
                .descendentTypeOf(DataSecurityHandler.class)
                .descendentTypeOf(PrincipalCustomizer.class);

        // Additional plugins requests
        for (SecurityProvider securityProvider : securityProviders) {
            securityProvider.classpathScanRequests(builder);
        }

        return builder.build();
    }

    @Override
    public Collection<BindingRequest> bindingRequests() {
        return bindingRequestsBuilder().descendentTypeOf(DataObfuscationHandler.class).build();
    }
}
