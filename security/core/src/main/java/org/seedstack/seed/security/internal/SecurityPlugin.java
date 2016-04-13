/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.BindingRequest;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;
import org.seedstack.seed.el.internal.ELPlugin;
import org.seedstack.seed.security.PrincipalCustomizer;
import org.seedstack.seed.security.Realm;
import org.seedstack.seed.security.RoleMapping;
import org.seedstack.seed.security.RolePermissionResolver;
import org.seedstack.seed.security.Scope;
import org.seedstack.seed.security.spi.SecurityScope;
import org.seedstack.seed.security.spi.data.DataObfuscationHandler;
import org.seedstack.seed.security.spi.data.DataSecurityHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    private final Map<String, Class<? extends Scope>> scopeClasses = new HashMap<String, Class<? extends Scope>>();
    private final Set<SecurityProvider> securityProviders = new HashSet<SecurityProvider>();
    private final Set<Class<? extends DataSecurityHandler<?>>> dataSecurityHandlers = new HashSet<Class<? extends DataSecurityHandler<?>>>();
    private SecurityConfigurer securityConfigurer;
    private boolean elDisabled;

    @Override
    public String name() {
        return "security";
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(ApplicationPlugin.class, ConfigurationProvider.class, ELPlugin.class, SecurityProvider.class);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .descendentTypeOf(Realm.class)
                .descendentTypeOf(RoleMapping.class)
                .descendentTypeOf(RolePermissionResolver.class)
                .descendentTypeOf(Scope.class)
                .descendentTypeOf(DataSecurityHandler.class)
                .descendentTypeOf(PrincipalCustomizer.class).build();
    }

    @Override
    public Collection<BindingRequest> bindingRequests() {
        return bindingRequestsBuilder().descendentTypeOf(DataObfuscationHandler.class).build();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public InitState init(InitContext initContext) {
        Configuration securityConfiguration = initContext.dependency(ConfigurationProvider.class).getConfiguration().subset(SECURITY_PREFIX);
        Map<Class<?>, Collection<Class<?>>> scannedClasses = initContext.scannedSubTypesByAncestorClass();

        Collection<Class<? extends PrincipalCustomizer<?>>> principalCustomizerClasses = (Collection) scannedClasses.get(PrincipalCustomizer.class);

        configureScopes(scannedClasses.get(Scope.class));
        configureDataSecurityHandlers(scannedClasses.get(DataSecurityHandler.class));
        securityProviders.addAll(initContext.dependencies(SecurityProvider.class));
        elDisabled = initContext.dependency(ELPlugin.class).isDisabled();
        securityConfigurer = new SecurityConfigurer(securityConfiguration, scannedClasses, principalCustomizerClasses);

        if (elDisabled) {
            LOGGER.info("No Java EL support, data security is disabled");
        }

        return InitState.INITIALIZED;
    }

    @SuppressWarnings("unchecked")
    private void configureDataSecurityHandlers(Collection<Class<?>> securityHandlerClasses) {
        if (securityHandlerClasses != null) {
            for (Class<?> candidate : securityHandlerClasses) {
                if (DataSecurityHandler.class.isAssignableFrom(candidate)) {
                    dataSecurityHandlers.add((Class<? extends DataSecurityHandler<?>>) candidate);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void configureScopes(Collection<Class<?>> scopeClasses) {
        if (scopeClasses != null) {
            for (Class<?> scopeCandidateClass : scopeClasses) {
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

                    if (this.scopeClasses.containsKey(scopeName)) {
                        throw SeedException.createNew(SecurityErrorCodes.DUPLICATE_SCOPE_NAME).put("scopeName", scopeName);
                    }

                    this.scopeClasses.put(scopeName, (Class<? extends Scope>) scopeCandidateClass);
                }
            }
        }
    }

    @Override
    public Object nativeUnitModule() {
        return new SecurityModule(
                securityConfigurer,
                scopeClasses,
                dataSecurityHandlers,
                elDisabled,
                securityProviders
        );
    }
}
