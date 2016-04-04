/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper;
import com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.kametic.specifications.Specification;
import org.seedstack.seed.SeedRuntime;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;
import org.seedstack.seed.rest.RelRegistry;
import org.seedstack.seed.rest.internal.exceptionmapper.AuthenticationExceptionMapper;
import org.seedstack.seed.rest.internal.exceptionmapper.AuthorizationExceptionMapper;
import org.seedstack.seed.rest.internal.exceptionmapper.InternalErrorExceptionMapper;
import org.seedstack.seed.rest.internal.exceptionmapper.WebApplicationExceptionMapper;
import org.seedstack.seed.rest.internal.hal.RelRegistryImpl;
import org.seedstack.seed.rest.internal.jsonhome.JsonHome;
import org.seedstack.seed.rest.internal.jsonhome.JsonHomeRootResource;
import org.seedstack.seed.rest.internal.jsonhome.Resource;
import org.seedstack.seed.rest.spi.RestProvider;
import org.seedstack.seed.rest.spi.RootResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import java.util.*;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class RestPlugin extends AbstractPlugin implements RestProvider {
    public static final Specification<Class<?>> resourcesSpecification = new JaxRsResourceSpecification();
    public static final Specification<Class<?>> providersSpecification = new JaxRsProviderSpecification();
    private static final Logger LOGGER = LoggerFactory.getLogger(RestPlugin.class);

    private final Map<Variant, Class<? extends RootResource>> rootResourcesByVariant = new HashMap<Variant, Class<? extends RootResource>>();
    private final RestConfiguration restConfiguration = new RestConfiguration();
    private boolean enabled = true;
    private ServletContext servletContext;
    private RelRegistry relRegistry;
    private JsonHome jsonHome;
    private Collection<Class<?>> resources;
    private Collection<Class<?>> providers;

    @Override
    public String name() {
        return "rest";
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        servletContext = ((SeedRuntime)containerContext).contextAs(ServletContext.class);
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(ConfigurationProvider.class);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .specification(providersSpecification)
                .specification(resourcesSpecification)
                .build();
    }

    @Override
    public InitState init(InitContext initContext) {

        restConfiguration.init(initContext.dependency(ConfigurationProvider.class).getConfiguration());

        Map<Specification, Collection<Class<?>>> scannedClasses = initContext.scannedTypesBySpecification();
        resources = scannedClasses.get(RestPlugin.resourcesSpecification);
        providers = scannedClasses.get(RestPlugin.providersSpecification);

        addJacksonProviders(providers);

        initializeHypermedia();

        if (servletContext == null) {
            enabled = false;
            LOGGER.info("No servlet context detected, REST support disabled");
            return InitState.INITIALIZED;
        }

        configureExceptionMappers();

        if (restConfiguration.isJsonHomeEnabled()) {
            // The typed locale variable resolves constructor ambiguity when the JAX-RS 2.0 spec is used
            Locale locale = null;
            addRootResourceVariant(new Variant(new MediaType("application", "json"), locale, null), JsonHomeRootResource.class);
        }

        return InitState.INITIALIZED;
    }

    private void configureExceptionMappers() {
        if (!restConfiguration.isSecurityExceptionMappingEnabled()) {
            providers.remove(AuthenticationExceptionMapper.class);
            providers.remove(AuthorizationExceptionMapper.class);
        }

        if (!restConfiguration.isExceptionMappingEnabled()) {
            providers.remove(WebApplicationExceptionMapper.class);
            providers.remove(InternalErrorExceptionMapper.class);
        }
    }

    private void addJacksonProviders(Collection<Class<?>> providers) {
        providers.add(JsonMappingExceptionMapper.class);
        providers.add(JsonParseExceptionMapper.class);
        providers.add(JacksonJsonProvider.class);
        providers.add(JacksonJaxbJsonProvider.class);
    }

    private void initializeHypermedia() {
        ResourceScanner resourceScanner = new ResourceScanner(restConfiguration, servletContext).scan(resources);
        Map<String, Resource> resourceMap = resourceScanner.jsonHomeResources();

        relRegistry = new RelRegistryImpl(resourceScanner.halLinks());
        jsonHome = new JsonHome(resourceMap);
    }

    @Override
    public Object nativeUnitModule() {
            return new AbstractModule() {
                @Override
                protected void configure() {
                    install(new HypermediaModule(jsonHome, relRegistry));
                    if (enabled) {
                        install(new RestModule(restConfiguration, resources, providers));
                        if (!rootResourcesByVariant.isEmpty()) {
                            install(new RootResourcesModule(rootResourcesByVariant));
                        }
                    }
                }
            };
    }

    public void addRootResourceVariant(Variant variant, Class<? extends RootResource> rootResource) {
        rootResourcesByVariant.put(variant, rootResource);
    }

    public RestConfiguration getConfiguration() {
        return restConfiguration;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Deprecated
    public void registerRootResource(Variant variant, Class<? extends RootResource> rootResource) {
        addRootResourceVariant(variant, rootResource);
    }

    @Deprecated
    public String getRestPath() {
        return restConfiguration.getRestPath();
    }

    @Deprecated
    public String getJspPath() {
        return restConfiguration.getJspPath();
    }

    @Override
    public Set<Class<?>> resources() {
        return resources != null ? new HashSet<Class<?>>(resources) : new HashSet<Class<?>>();
    }

    @Override
    public Set<Class<?>> providers() {
        return providers != null ? new HashSet<Class<?>>(providers) : new HashSet<Class<?>>();
    }
}
