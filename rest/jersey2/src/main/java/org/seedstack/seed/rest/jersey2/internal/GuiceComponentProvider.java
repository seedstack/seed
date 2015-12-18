/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.internal;

import com.google.inject.Injector;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.ServiceBindingBuilder;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.server.spi.ComponentProvider;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.seedstack.seed.SeedException;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.util.Set;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@javax.ws.rs.ext.Provider
public class GuiceComponentProvider implements ComponentProvider {

    private ServiceLocator serviceLocator;
    private Injector injector;

    @Override
    public void initialize(ServiceLocator locator) {
        prepareGuiceHK2Bridge(locator);
    }

    private void prepareGuiceHK2Bridge(ServiceLocator locator) {
        setServiceLocatorAndInjector(locator);

        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
        GuiceIntoHK2Bridge guiceBridge = locator.getService(GuiceIntoHK2Bridge.class);
        guiceBridge.bridgeGuiceInjector(injector);
    }

    private void setServiceLocatorAndInjector(ServiceLocator locator) {
        this.serviceLocator = locator;

        injector = (Injector) getServletContext().getAttribute(Injector.class.getName());
        if (injector == null) {
            throw SeedException.createNew(Jersey2ErrorCodes.MISSING_INJECTOR);
        }
    }

    private ServletContext getServletContext() {
        ServletContext servletContext = serviceLocator.getService(ServletContext.class);
        if (servletContext == null) {
            throw SeedException.createNew(Jersey2ErrorCodes.MISSING_SERVLET_CONTEXT);
        }
        return servletContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean bind(Class<?> component, Set<Class<?>> providerContracts) {
        boolean isBound = false;
        if (isJaxRsClass(component)) {
            registerBindingsInHK2(component, providerContracts);
            isBound = true;
        }
        return isBound;
    }

    private void registerBindingsInHK2(Class<?> componentClass, Set<Class<?>> providerContracts) {
        ServiceBindingBuilder componentBindingBuilder = getBindingBuilder(componentClass);
        //noinspection unchecked
        componentBindingBuilder.to(componentClass);
        bindProviderContracts(componentBindingBuilder, providerContracts);

        DynamicConfiguration dynamicConfiguration = Injections.getConfiguration(serviceLocator);
        Injections.addBinding(componentBindingBuilder, dynamicConfiguration);
        dynamicConfiguration.commit();
    }

    private ServiceBindingBuilder getBindingBuilder(Class<?> component) {
        GuiceToHK2Factory guiceFactory = new GuiceToHK2Factory(component, injector, serviceLocator);
        return Injections.newFactoryBinder(guiceFactory);
    }

    private void bindProviderContracts(ServiceBindingBuilder componentBindingBuilder, Set<Class<?>> providerContracts) {
        if (providerContracts != null) {
            for (Class<?> providerContract : providerContracts) {
                //noinspection unchecked
                componentBindingBuilder.to(providerContract);
            }
        }
    }

    private boolean isJaxRsClass(Class<?> component) {
        return component.isAnnotationPresent(Path.class) || component.isAnnotationPresent(Provider.class);
    }

    @Override
    public void done() {
    }
}
