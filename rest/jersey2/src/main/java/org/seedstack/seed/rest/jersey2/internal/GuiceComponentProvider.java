/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.jersey2.internal;

import com.google.inject.Injector;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.BindingBuilder;
import org.glassfish.hk2.utilities.binding.BindingBuilderFactory;
import org.glassfish.hk2.utilities.binding.ServiceBindingBuilder;
import org.glassfish.jersey.inject.hk2.DelayedHk2InjectionManager;
import org.glassfish.jersey.inject.hk2.ImmediateHk2InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.spi.ComponentProvider;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.web.internal.ServletContextUtils;

public class GuiceComponentProvider implements ComponentProvider {
    private ServiceLocator serviceLocator;
    private Injector injector;

    public void initialize(InjectionManager injectionManager) {
        if (injectionManager instanceof ImmediateHk2InjectionManager) {
            initialize(((ImmediateHk2InjectionManager) injectionManager).getServiceLocator());
        } else if (injectionManager instanceof DelayedHk2InjectionManager) {
            initialize(((DelayedHk2InjectionManager) injectionManager).getServiceLocator());
        } else {
            throw SeedException.createNew(Jersey2ErrorCode.UNSUPPORTED_JERSEY_DEPENDENCY_INJECTION);
        }
    }

    void initialize(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
        this.injector = ServletContextUtils.getInjector(getServletContext());
        if (injector == null) {
            throw SeedException.createNew(Jersey2ErrorCode.MISSING_INJECTOR);
        }
        GuiceBridge.getGuiceBridge().initializeGuiceBridge(this.serviceLocator);
        this.serviceLocator.getService(GuiceIntoHK2Bridge.class).bridgeGuiceInjector(this.injector);
    }

    @Override
    public boolean bind(Class<?> component, Set<Class<?>> providerContracts) {
        boolean isBound = false;
        if (isJaxRsClass(component)) {
            registerBindingsInHK2(component, providerContracts);
            isBound = true;
        }
        return isBound;
    }

    @Override
    public void done() {
        // nothing to do
    }

    private ServletContext getServletContext() {
        ServletContext servletContext = serviceLocator.getService(ServletContext.class);
        if (servletContext == null) {
            throw SeedException.createNew(Jersey2ErrorCode.MISSING_SERVLET_CONTEXT);
        }
        return servletContext;
    }

    private boolean isJaxRsClass(Class<?> component) {
        return component.isAnnotationPresent(Path.class) || component.isAnnotationPresent(Provider.class);
    }

    private <T> void registerBindingsInHK2(Class<T> componentClass, Set<Class<?>> providerContracts) {
        ServiceBindingBuilder<T> componentBindingBuilder = getBindingBuilder(componentClass);
        componentBindingBuilder.to(componentClass);
        bindProviderContracts(componentBindingBuilder, providerContracts);

        DynamicConfiguration dynamicConfiguration = getConfiguration(serviceLocator);
        addBinding(componentBindingBuilder, dynamicConfiguration);
        dynamicConfiguration.commit();
    }

    private <T> ServiceBindingBuilder<T> getBindingBuilder(Class<T> component) {
        return newFactoryBinder(new GuiceToHK2Factory<>(component, injector, serviceLocator));
    }

    private <T> void bindProviderContracts(ServiceBindingBuilder<T> componentBindingBuilder,
            Set<Class<?>> providerContracts) {
        if (providerContracts != null) {
            for (Class<?> providerContract : providerContracts) {
                componentBindingBuilder.to(providerContract);
            }
        }
    }

    /**
     * Add a binding represented by the binding builder to the HK2 dynamic configuration.
     *
     * @param builder       binding builder.
     * @param configuration HK2 dynamic configuration.
     */
    private static void addBinding(final BindingBuilder<?> builder, final DynamicConfiguration configuration) {
        BindingBuilderFactory.addBinding(builder, configuration);
    }

    /**
     * Get service locator {@link DynamicConfiguration dynamic configuration}.
     *
     * @param locator HK2 service locator.
     * @return dynamic configuration for a given service locator.
     */
    private static DynamicConfiguration getConfiguration(final ServiceLocator locator) {
        final DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        return dcs.createDynamicConfiguration();
    }

    /**
     * Get a new factory instance-based service binding builder.
     *
     * @param <T>     service type.
     * @param factory service instance.
     * @return initialized binding builder.
     */
    private static <T> ServiceBindingBuilder<T> newFactoryBinder(final Factory<T> factory) {
        return BindingBuilderFactory.newFactoryBinder(factory);
    }
}
