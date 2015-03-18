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

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.PrivateElements;
import com.google.inject.util.Modules;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.security.api.PrincipalCustomizer;
import org.seedstack.seed.security.api.Realm;
import org.seedstack.seed.security.api.RoleMapping;
import org.seedstack.seed.security.api.RolePermissionResolver;
import org.seedstack.seed.security.internal.configure.SeedSecurityConfigurer;
import org.seedstack.seed.security.internal.data.DataSecurityModule;
import org.seedstack.seed.security.internal.securityexpr.SecurityExpressionModule;
import org.seedstack.seed.security.spi.SecurityConcern;
import org.seedstack.seed.security.spi.data.DataObfuscationHandler;
import org.seedstack.seed.security.spi.data.DataSecurityHandler;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.BindingRequest;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequestBuilder;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.mgt.SecurityManager;
import org.kametic.specifications.Specification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * This plugin provides core security infrastructure, based on Apache Shiro
 * implementation.
 * 
 * @author yves.dautremay@mpsa.com
 */
public class SeedSecurityCorePlugin extends AbstractPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeedSecurityCorePlugin.class);

    public static final String SECURITY_PREFIX = "org.seedstack.seed.security";

    private Configuration securityConfiguration;

    private Map<Class<?>, Collection<Class<?>>> scannedClasses;

    private Collection<SeedSecurityPlugin> securityPlugins = new ArrayList<SeedSecurityPlugin>();
    
    private final Specification<Class<?>> specificationDataSecurityHandlers = classImplements(DataSecurityHandler.class);
    private final Specification<Class<?>> specificationDataObfuscationHandlers = classImplements(DataObfuscationHandler.class);
    
    private Collection<Class<? extends DataSecurityHandler<?>>> dataSecurityHandlers;
    private Collection<Class<?>> principalCustomizerClasses;


    /**
     * Plugin constructor
     */
    public SeedSecurityCorePlugin() {
        ServiceLoader<SeedSecurityPlugin> loader = ServiceLoader.load(SeedSecurityPlugin.class, SeedReflectionUtils.findMostCompleteClassLoader(SeedSecurityCorePlugin.class));
        Iterator<SeedSecurityPlugin> it = loader.iterator();
        if (!it.hasNext()) {
            securityPlugins.add(new DefaultSecurityPlugin());
        } else {
            while (it.hasNext()) {
                securityPlugins.add(it.next());
            }
        }
    }

    @Override
    public String name() {
        return "seed-security-plugin";
    }

    
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public InitState init(InitContext initContext) {
        ApplicationPlugin applicationPlugin = (ApplicationPlugin) initContext.pluginsRequired().iterator().next();
        securityConfiguration = applicationPlugin.getApplication().getConfiguration().subset(SECURITY_PREFIX);
        scannedClasses = initContext.scannedSubTypesByAncestorClass();
        principalCustomizerClasses = initContext.scannedSubTypesByParentClass().get(PrincipalCustomizer.class);
        
        Map<Specification, Collection<Class<?>>> scannedTypesBySpecification = initContext.scannedTypesBySpecification();
        dataSecurityHandlers =  (Collection) scannedTypesBySpecification.get(specificationDataSecurityHandlers);

        for (SeedSecurityPlugin securityPlugin : securityPlugins) {
            LOGGER.debug("Loading security plugin {}", securityPlugin.getClass().getCanonicalName());
            securityPlugin.init(initContext);
        }
        return InitState.INITIALIZED;
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        for (SeedSecurityPlugin securityPlugin : securityPlugins) {
            securityPlugin.provideContainerContext(containerContext);
        }
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        return plugins;
    }

    @Override
    public Object nativeUnitModule() {
        return new SeedSecurityModule();
    }

    private Collection<Module> getCoreModules() {
        SeedSecurityConfigurer configurer = new SeedSecurityConfigurer(securityConfiguration, scannedClasses, principalCustomizerClasses);
        Collection<Module> coreModules = new ArrayList<Module>();
        coreModules.add(new SeedSecurityCoreModule(configurer));
        coreModules.add(new SeedSecurityCoreAopModule());
        coreModules.add(new DataSecurityModule(dataSecurityHandlers));
        coreModules.add(new SecurityExpressionModule());
        return coreModules;
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        ClasspathScanRequestBuilder builder = classpathScanRequestBuilder().descendentTypeOf(Realm.class)
                .descendentTypeOf(RoleMapping.class)
                .descendentTypeOf(RolePermissionResolver.class)
                .specification(specificationDataSecurityHandlers)
                .specification(specificationDataObfuscationHandlers)
                .subtypeOf(PrincipalCustomizer.class);
        for (SeedSecurityPlugin securityPlugin : securityPlugins) {
            securityPlugin.classpathScanRequests(builder);
        }
        return builder.build();
    }
    
    

    @Override
	public Collection<BindingRequest> bindingRequests() {
		return bindingRequestsBuilder().specification(specificationDataObfuscationHandlers).build();
	}



	@SecurityConcern
    private class SeedSecurityModule extends AbstractModule {
        @Override
        protected void configure() {
            for (Module module : getCoreModules()) {
                install(module);
            }
            Collection<Module> shiroModules = new ArrayList<Module>();
            for (SeedSecurityPlugin securityPlugin : securityPlugins) {
                Module shiroModule = securityPlugin.provideShiroModule();
                if (shiroModule != null) {
                    shiroModules.add(removeSecurityManager(shiroModule));
                }
                install(Modules.combine(securityPlugin.provideOtherModules()));
            }
            install(Modules.combine(shiroModules));
        }
    }

    private Module removeSecurityManager(Module module) {
        List<Element> elements = Elements.getElements(module);
        // ShiroModule is only a private module
        final PrivateElements privateElements = (PrivateElements) elements.iterator().next();

        return new PrivateModule() {
            @Override
            protected void configure() {
                for (Element element : privateElements.getElements()) {
                    element.applyTo(binder());
                }
                for (Key<?> exposedKey : privateElements.getExposedKeys()) {
                    if (!exposedKey.equals(Key.get(SecurityManager.class))) {
                        expose(exposedKey);
                    }
                }
            }
        };
    }
}
