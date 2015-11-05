/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.application;

import com.google.common.collect.Sets;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import jodd.props.Props;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;
import org.javatuples.Pair;
import org.seedstack.seed.Application;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CorePlugin;
import org.seedstack.seed.spi.configuration.ConfigurationLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin that initializes the application identity, storage location and configuration.
 *
 * @author adrien.lauer@mpsa.com
 */
public class ApplicationPlugin extends AbstractPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationPlugin.class);

    public static final String CONFIGURATION_PACKAGE = "META-INF.configuration";
    public static final String CONFIGURATION_LOCATION = "META-INF/configuration/";
    public static final String PROPS_REGEX = ".*\\.props";
    public static final String PROPERTIES_REGEX = ".*\\.properties";
    public static final String BASE_PACKAGES_KEY = "org.seedstack.seed.base-packages";

    private final SeedConfigLoader seedConfigLoader = new SeedConfigLoader();
    private final Configuration bootstrapConfiguration = seedConfigLoader.buildBootstrapConfig();
    private final Map<String, String> defaultConfiguration = new ConcurrentHashMap<String, String>();
    private Props props;
    private Application application;

    @Override
    public String name() {
        return "seed-core-application-plugin";
    }

    @Override
    public String pluginPackageRoot() {
        String packageRoots = CONFIGURATION_PACKAGE;

        String[] applicationPackageRoots = bootstrapConfiguration.getStringArray(BASE_PACKAGES_KEY);
        if (applicationPackageRoots != null && applicationPackageRoots.length > 0) {
            packageRoots += "," + StringUtils.join(applicationPackageRoots, ",");
        }

        return packageRoots;
    }

    @Override
    public InitState init(InitContext initContext) {
        // Setup application diagnostic collector
        ApplicationDiagnosticCollector applicationDiagnosticCollector = new ApplicationDiagnosticCollector();
        applicationDiagnosticCollector.setBasePackages(pluginPackageRoot());
        ((CorePlugin) initContext.pluginsRequired().iterator().next()).registerDiagnosticCollector("org.seedstack.seed.core.application", applicationDiagnosticCollector);

        // Retrieve all configuration resources
        Set<String> allConfigurationResources = Sets.newHashSet();
        for (String propertiesResource : initContext.mapResourcesByRegex().get(PROPERTIES_REGEX)) {
            if (propertiesResource.startsWith(CONFIGURATION_LOCATION)) {
                allConfigurationResources.add(propertiesResource);
            }
        }
        for (String propsResource : initContext.mapResourcesByRegex().get(PROPS_REGEX)) {
            if (propsResource.startsWith(CONFIGURATION_LOCATION)) {
                allConfigurationResources.add(propsResource);
            }
        }

        // Find configuration lookups
        Map<String, Class<? extends StrLookup>> configurationLookups = new HashMap<String, Class<? extends StrLookup>>();
        for (Class<?> candidate : initContext.scannedClassesByAnnotationClass().get(ConfigurationLookup.class)) {
            ConfigurationLookup configurationLookup = candidate.getAnnotation(ConfigurationLookup.class);
            if (StrLookup.class.isAssignableFrom(candidate) && configurationLookup != null && !configurationLookup.value().isEmpty()) {
                configurationLookups.put(configurationLookup.value(), candidate.asSubclass(StrLookup.class));
                LOGGER.trace("Detected configuration lookup {}", configurationLookup.value());
            }
        }

        // Build configuration
        Pair<MapConfiguration, Props> confs = seedConfigLoader.buildConfig(allConfigurationResources, defaultConfiguration);
        MapConfiguration configuration = confs.getValue0();
        props = confs.getValue1();

        // Determine configuration profile
        String[] profiles = seedConfigLoader.applicationProfiles();
        if (profiles == null || profiles.length == 0) {
            LOGGER.info("No configuration profile selected");
            applicationDiagnosticCollector.setActiveProfiles("");
        } else {
            String activeProfiles = Arrays.toString(profiles);
            LOGGER.info("Active configuration profile(s): {}", activeProfiles);
            applicationDiagnosticCollector.setActiveProfiles(activeProfiles);
        }

        applicationDiagnosticCollector.setConfiguration(configuration);

        Configuration coreConfiguration = configuration.subset(CorePlugin.CORE_PLUGIN_PREFIX);

        String appId = coreConfiguration.getString("application-id");
        if (appId == null || appId.isEmpty()) {
            throw SeedException.createNew(ApplicationErrorCode.MISSING_APPLICATION_IDENTIFIER).put("property",
                    CorePlugin.CORE_PLUGIN_PREFIX + ".application-id");
        }

        String appName = coreConfiguration.getString("application-name");
        if (appName == null) {
            appName = appId;
        }

        String appVersion = coreConfiguration.getString("application-version");
        if (appVersion == null) {
            appVersion = "1.0.0";
        }

        LOGGER.info("Application info: '{}' / '{}' / '{}'", appId, appName, appVersion);
        applicationDiagnosticCollector.setApplicationId(appId);
        applicationDiagnosticCollector.setApplicationName(appName);
        applicationDiagnosticCollector.setApplicationVersion(appVersion);

        File seedDirectory = registerApplicationStorage(coreConfiguration, appId);

        LOGGER.debug("Application storage location is {}", seedDirectory.getAbsolutePath());
        applicationDiagnosticCollector.setStorageLocation(seedDirectory.getAbsolutePath());

        if (coreConfiguration.getBoolean("redirect-jul", true)) {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            LOGGER.debug("Java logging to SLF4J redirection enabled, if you're using logback be sure to have a LevelChangePropagator in your configuration");
        }

        ApplicationImpl application = new ApplicationImpl(appName, appId, appVersion, seedDirectory, configuration);

        // Register configuration lookups
        for (Map.Entry<String, Class<? extends StrLookup>> configurationLookup : configurationLookups.entrySet()) {
            configuration.getInterpolator().registerLookup(configurationLookup.getKey(), buildStrLookup(configurationLookup.getValue(), application));
        }

        this.application = application;

        return InitState.INITIALIZED;
    }

    private File registerApplicationStorage(Configuration coreConfiguration, String appId) {
        String seedStorage = coreConfiguration.getString("storage");
        File seedDirectory;
        if (seedStorage == null) {
            seedDirectory = new File(new File(getUserHome(), ".seed"), appId);
        } else {
            seedDirectory = new File(seedStorage);
        }

        if (!seedDirectory.exists() && !seedDirectory.mkdirs()) {
            throw SeedException.createNew(ApplicationErrorCode.UNABLE_TO_CREATE_STORAGE_DIRECTORY).put("path", seedDirectory.getAbsolutePath());
        }

        if (!seedDirectory.isDirectory()) {
            throw SeedException.createNew(ApplicationErrorCode.STORAGE_PATH_IS_NOT_A_DIRECTORY).put("path", seedDirectory.getAbsolutePath());
        }

        if (!seedDirectory.canWrite()) {
            throw SeedException.createNew(ApplicationErrorCode.STORAGE_DIRECTORY_IS_NOT_WRITABLE).put("path", seedDirectory.getAbsolutePath());
        }
        return seedDirectory;
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().resourcesRegex(PROPERTIES_REGEX).resourcesRegex(PROPS_REGEX).annotationType(ConfigurationLookup.class).build();
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(CorePlugin.class);
        return plugins;
    }

    @Override
    public Object nativeUnitModule() {
        return new ApplicationModule(this.application);
    }

    /**
     * Retrieve the application object.
     *
     * @return the application object.
     */
    public Application getApplication() {
        return this.application;
    }

    /**
     * Return the inner props configuration.
     *
     * @return the inner props configuration.
     */
    public Props getProps() {
        return this.props;
    }

    /**
     * Return the default configuration.
     *
     * @return the default configuration.
     */
    public Map<String, String> getDefaultConfiguration() {
        return defaultConfiguration;
    }

    private String getUserHome() {
        String userProfile = System.getenv("USERPROFILE");
        if (userProfile != null) {
            return userProfile;
        } else {
            return System.getProperty("user.home");
        }
    }

    private StrLookup buildStrLookup(Class<? extends StrLookup> strLookupClass, Application application) {
        try {
            try {
                return strLookupClass.getConstructor(Application.class).newInstance(application);
            } catch (NoSuchMethodException e1) {
                try {
                    return strLookupClass.getConstructor().newInstance();
                } catch (NoSuchMethodException e2) {
                    throw SeedException.wrap(e2, ApplicationErrorCode.NO_SUITABLE_CONFIGURATION_LOOKUP_CONSTRUCTOR_FOUND).put("className", strLookupClass.getCanonicalName());
                }
            }
        } catch (Exception e) {
            throw SeedException.wrap(e, ApplicationErrorCode.UNABLE_TO_INSTANTIATE_CONFIGURATION_LOOKUP).put("className", strLookupClass.getCanonicalName());
        }
    }
}
