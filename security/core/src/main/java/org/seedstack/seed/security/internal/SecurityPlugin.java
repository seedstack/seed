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
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.core.internal.el.ELPlugin;
import org.seedstack.seed.security.PrincipalCustomizer;
import org.seedstack.seed.security.Realm;
import org.seedstack.seed.security.RoleMapping;
import org.seedstack.seed.security.RolePermissionResolver;
import org.seedstack.seed.security.Scope;
import org.seedstack.seed.security.SecurityConfig;
import org.seedstack.seed.security.spi.CrudActionResolver;
import org.seedstack.seed.security.spi.SecurityScope;
import org.seedstack.shed.misc.PriorityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.seedstack.shed.misc.PriorityUtils.sortByPriority;

/**
 * This plugin provides core security infrastructure, based on Apache Shiro
 * implementation.
 */
public class SecurityPlugin extends AbstractSeedPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityPlugin.class);
    private final Map<String, Class<? extends Scope>> scopeClasses = new HashMap<>();
    private final Set<SecurityProvider> securityProviders = new HashSet<>();
    private final List<Class<? extends CrudActionResolver>> crudActionResolvers = new ArrayList<>();
    private SecurityConfigurer securityConfigurer;
    private boolean elAvailable;

    @Override
    public String name() {
        return "security";
    }

    @Override
    public Collection<Class<?>> dependencies() {
        return Lists.newArrayList(ELPlugin.class, SecurityProvider.class);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .descendentTypeOf(Realm.class)
                .descendentTypeOf(RoleMapping.class)
                .descendentTypeOf(RolePermissionResolver.class)
                .descendentTypeOf(Scope.class)
                .descendentTypeOf(PrincipalCustomizer.class)
                .descendentTypeOf(CrudActionResolver.class)
                .build();
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public InitState initialize(InitContext initContext) {
        SecurityConfig securityConfig = getConfiguration(SecurityConfig.class);
        Map<Class<?>, Collection<Class<?>>> scannedClasses = initContext.scannedSubTypesByAncestorClass();

        configureScopes(scannedClasses.get(Scope.class));
        configureCrudActionResolvers(scannedClasses.get(CrudActionResolver.class));

        securityProviders.addAll(initContext.dependencies(SecurityProvider.class));

        elAvailable = initContext.dependency(ELPlugin.class).isFunctionMappingAvailable();

        securityConfigurer = new SecurityConfigurer(
                securityConfig,
                scannedClasses,
                (Collection) scannedClasses.get(PrincipalCustomizer.class)
        );

        return InitState.INITIALIZED;
    }

    @SuppressWarnings("unchecked")
    private void configureCrudActionResolvers(Collection<Class<?>> candidates) {
        if (candidates != null) {
            candidates.stream()
                    .map(x -> (Class<? extends CrudActionResolver>) x)
                    .forEach(resolver -> {
                        crudActionResolvers.add(resolver);
                        LOGGER.trace("Detected CRUD action resolver {}", resolver.getName());
                    });
            sortByPriority(crudActionResolvers, PriorityUtils::priorityOfClassOf);
        }
        LOGGER.debug("Detected {} CRUD action resolver(s)", crudActionResolvers.size());
    }

    @SuppressWarnings("unchecked")
    private void configureScopes(Collection<Class<?>> candidates) {
        if (candidates != null) {
            for (Class<?> candidate : candidates) {
                if (Scope.class.isAssignableFrom(candidate)) {
                    SecurityScope securityScope = candidate.getAnnotation(SecurityScope.class);
                    String scopeName;

                    if (securityScope != null) {
                        scopeName = securityScope.value();
                    } else {
                        scopeName = candidate.getSimpleName();
                    }

                    try {
                        candidate.getConstructor(String.class);
                    } catch (NoSuchMethodException e) {
                        throw SeedException.wrap(e, SecurityErrorCode.MISSING_ADEQUATE_SCOPE_CONSTRUCTOR)
                                .put("scopeName", scopeName)
                                .put("class", candidate.getName());
                    }

                    if (scopeClasses.containsKey(scopeName)) {
                        throw SeedException.createNew(SecurityErrorCode.DUPLICATE_SCOPE_NAME)
                                .put("scopeName", scopeName)
                                .put("class1", scopeClasses.get(scopeName).getName())
                                .put("class2", candidate.getName());
                    }

                    LOGGER.trace("Detected security scope '{}' implemented by {}", scopeName, candidate.getName());
                    scopeClasses.put(scopeName, (Class<? extends Scope>) candidate);
                }
            }
        }
        LOGGER.debug("Detected {} security scope(s)", scopeClasses.size());
    }

    @Override
    public Object nativeUnitModule() {
        return new SecurityModule(
                securityConfigurer,
                scopeClasses,
                elAvailable,
                securityProviders,
                crudActionResolvers);
    }
}
