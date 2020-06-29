/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
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
import com.google.inject.AbstractModule;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.core.internal.init.ValidationManager;
import org.seedstack.seed.rest.RelRegistry;
import org.seedstack.seed.rest.RestConfig;
import org.seedstack.seed.rest.internal.exceptionmapper.AuthenticationExceptionMapper;
import org.seedstack.seed.rest.internal.exceptionmapper.AuthorizationExceptionMapper;
import org.seedstack.seed.rest.internal.exceptionmapper.InternalErrorExceptionMapper;
import org.seedstack.seed.rest.internal.exceptionmapper.ValidationExceptionMapper;
import org.seedstack.seed.rest.internal.exceptionmapper.WebApplicationExceptionMapper;
import org.seedstack.seed.rest.internal.hal.RelRegistryImpl;
import org.seedstack.seed.rest.internal.jsonhome.JsonHome;
import org.seedstack.seed.rest.internal.jsonhome.JsonHomeRootResource;
import org.seedstack.seed.rest.internal.jsonhome.Resource;
import org.seedstack.seed.rest.spi.RestProvider;
import org.seedstack.seed.rest.spi.RootResource;
import org.seedstack.seed.spi.ConfigurationPriority;
import org.seedstack.shed.reflect.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestPlugin extends AbstractSeedPlugin implements RestProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestPlugin.class);
    private final Map<Variant, Class<? extends RootResource>> rootResourcesByVariant = new HashMap<>();
    private boolean enabled;
    private RestConfig restConfig;
    private RelRegistry relRegistry;
    private JsonHome jsonHome;
    private Collection<Class<?>> resources;
    private Collection<Class<?>> providers;
    private SeedRuntime seedRuntime;

    @Override
    public String name() {
        return "rest";
    }

    @Override
    protected void setup(SeedRuntime seedRuntime) {
        this.seedRuntime = seedRuntime;
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .predicate(JaxRsResourcePredicate.INSTANCE)
                .predicate(JaxRsProviderPredicate.INSTANCE)
                .build();
    }

    @Override
    public InitState initialize(InitContext initContext) {
        ServletContext servletContext = seedRuntime.contextAs(ServletContext.class);
        Map<Predicate<Class<?>>, Collection<Class<?>>> scannedClasses = initContext.scannedTypesByPredicate();

        restConfig = getConfiguration(RestConfig.class);
        resources = scannedClasses.get(JaxRsResourcePredicate.INSTANCE);
        providers = scannedClasses.get(JaxRsProviderPredicate.INSTANCE);

        if (servletContext != null) {
            addJacksonProviders(providers);

            configureExceptionMappers();
            configureStreamSupport();

            initializeHypermedia(servletContext.getContextPath());

            if (restConfig.isJsonHome()) {
                // The typed locale parameter resolves constructor ambiguity when the JAX-RS 2.0 spec is in the
                // classpath
                addRootResourceVariant(new Variant(new MediaType("application", "json"), (Locale) null, null),
                        JsonHomeRootResource.class);
            }

            seedRuntime.registerConfigurationProvider(
                    new RestRuntimeConfigurationProvider(restConfig),
                    ConfigurationPriority.RUNTIME_INFO
            );

            enabled = true;
        } else {
            initializeHypermedia("");
        }

        return InitState.INITIALIZED;
    }

    private void configureExceptionMappers() {
        if (!restConfig.exceptionMapping().isSecurity()) {
            providers.remove(AuthenticationExceptionMapper.class);
            providers.remove(AuthorizationExceptionMapper.class);
            LOGGER.debug("Security exception mapping is disabled");
        } else {
            LOGGER.debug("Security exception mapping is enabled");
        }

        if (!restConfig.exceptionMapping().isAll()) {
            providers.remove(WebApplicationExceptionMapper.class);
            providers.remove(InternalErrorExceptionMapper.class);
            LOGGER.debug("Default exception mapping is disabled");
        } else {
            LOGGER.debug("Default exception mapping is enabled");
        }

        if (!isDynamicValidationSupported() || !restConfig.exceptionMapping().isValidation()) {
            providers.remove(ValidationExceptionMapper.class);
            LOGGER.debug("Validation exception mapping is disabled");
        } else {
            LOGGER.debug("Validation exception mapping is enabled");
        }
    }

    private boolean isDynamicValidationSupported() {
        return ValidationManager.get().getValidationLevel().compareTo(ValidationManager.ValidationLevel.LEVEL_1_1) >= 0;
    }

    private void configureStreamSupport() {
        if (!restConfig.isStreamSupport()) {
            providers.remove(StreamMessageBodyReader.class);
            providers.remove(StreamMessageBodyWriter.class);
            LOGGER.debug("Stream support is disabled");
        } else {
            LOGGER.debug("Stream support is enabled");
        }
    }

    private void addJacksonProviders(Collection<Class<?>> providers) {
        providers.add(JsonMappingExceptionMapper.class);
        providers.add(JsonParseExceptionMapper.class);
        if (Classes.optional("javax.xml.bind.JAXBElement").isPresent()) {
            LOGGER.debug("JSON/XML support is enabled");
            providers.add(JacksonJaxbJsonProvider.class);
        } else {
            LOGGER.debug("JSON support is enabled");
            providers.add(JacksonJsonProvider.class);
        }
    }

    private void initializeHypermedia(String contextPath) {
        ResourceScanner resourceScanner = new ResourceScanner(restConfig, contextPath).scan(resources);
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
                    install(new RestModule(filterResourceClasses(resources), providers, rootResourcesByVariant));
                }
            }
        };
    }

    private Collection<Class<?>> filterResourceClasses(Collection<Class<?>> resourceClasses) {
        if (!rootResourcesByVariant.isEmpty()) {
            return resourceClasses;
        } else {
            HashSet<Class<?>> filteredResourceClasses = new HashSet<>(resourceClasses);
            filteredResourceClasses.remove(RootResourceDispatcher.class);
            return filteredResourceClasses;
        }
    }

    public void addRootResourceVariant(Variant variant, Class<? extends RootResource> rootResource) {
        rootResourcesByVariant.put(variant, rootResource);
    }

    public RestConfig getRestConfig() {
        return restConfig;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Set<Class<?>> resources() {
        return resources != null ? new HashSet<>(filterResourceClasses(resources)) : new HashSet<>();
    }

    @Override
    public Set<Class<?>> providers() {
        return providers != null ? new HashSet<>(providers) : new HashSet<>();
    }
}
