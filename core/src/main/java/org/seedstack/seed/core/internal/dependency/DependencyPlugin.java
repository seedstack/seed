/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.dependency;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.seedstack.seed.spi.DependencyProvider;
import org.seedstack.shed.reflect.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DependencyPlugin extends AbstractSeedPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyPlugin.class);
    private final Map<Class<?>, Optional<? extends DependencyProvider>> dependencies = new HashMap<>();

    @Override
    public String name() {
        return "dependency";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .subtypeOf(DependencyProvider.class)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected InitState initialize(InitContext initContext) {
        initContext.scannedSubTypesByParentClass().get(DependencyProvider.class)
                .stream()
                .filter(DependencyProvider.class::isAssignableFrom)
                .forEach(candidate -> getDependency((Class<DependencyProvider>) candidate));

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new DependencyModule(dependencies);
    }

    /**
     * Return {@link Optional} which contains the provider if dependency is present.
     * Always return a {@link Optional} instance.
     *
     * @param providerClass provider to use an optional dependency
     * @return {@link Optional} which contains the provider if dependency is present
     */
    @SuppressWarnings("unchecked")
    public <T extends DependencyProvider> Optional<T> getDependency(Class<T> providerClass) {
        if (!dependencies.containsKey(providerClass)) {
            Optional<T> optionalDependency = Optional.empty();
            try {
                T provider = providerClass.newInstance();
                if (Classes.optional(provider.getClassToCheck()).isPresent()) {
                    LOGGER.debug("Found a new optional provider [{}] for [{}]", providerClass.getName(),
                            provider.getClassToCheck());
                    optionalDependency = Optional.of(provider);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_INSTANTIATE_CLASS).put("class",
                        providerClass.getCanonicalName());
            }
            dependencies.put(providerClass, optionalDependency);
        }
        return (Optional<T>) dependencies.get(providerClass);
    }
}
