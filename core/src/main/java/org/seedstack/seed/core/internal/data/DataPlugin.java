/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.data;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.kametic.specifications.Specification;
import org.seedstack.seed.*;
import org.seedstack.seed.core.internal.CorePlugin;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This plugin provides data import and export facilities.
 *
 * @author pierre.thirouin@ext.mpsa.com
 */
public class DataPlugin extends AbstractPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataPlugin.class);

    private final Specification<Class<?>> dataExporterSpecification = and(classImplements(DataExporter.class), classAnnotatedWith(DataSet.class));
    private final Specification<Class<?>> dataImporterSpecification = and(classImplements(DataImporter.class), classAnnotatedWith(DataSet.class));

    private final Map<String, Map<String, DataExporterDefinition<Object>>> allDataExporters = new HashMap<String, Map<String, DataExporterDefinition<Object>>>();
    private final Map<String, Map<String, DataImporterDefinition<Object>>> allDataImporters = new HashMap<String, Map<String, DataImporterDefinition<Object>>>();

    private boolean loadInitializationData;
    private boolean forceInitializationData;

    @Inject
    private DataManager dataManager;

    @Override
    public String name() {
        return "seed-data-plugin";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().specification(dataExporterSpecification).specification(dataImporterSpecification).build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState init(InitContext initContext) {
        Configuration configuration = initContext.dependency(ConfigurationProvider.class)
                .getConfiguration().subset(CorePlugin.CORE_PLUGIN_PREFIX);

        String dataInitializationMode = configuration.getString("data-initialization", "auto");
        if ("auto".equals(dataInitializationMode)) {
            loadInitializationData = true;
        } else if ("force".equals(dataInitializationMode)) {
            loadInitializationData = true;
            forceInitializationData = true;
        }

        Collection<Class<?>> scannedDataExporterClasses = initContext.scannedTypesBySpecification().get(dataExporterSpecification);
        for (Class<?> scannedDataExporterClass : scannedDataExporterClasses) {
            if (DataExporter.class.isAssignableFrom(scannedDataExporterClass)) {
                DataSet dataSet = scannedDataExporterClass.getAnnotation(DataSet.class);
                Map<String, DataExporterDefinition<Object>> nameDataExporterDefinitionMap = allDataExporters.get(dataSet.group());
                if (nameDataExporterDefinitionMap == null) {
                    nameDataExporterDefinitionMap = new HashMap<String, DataExporterDefinition<Object>>();
                }

                Class exportedClass = getTypeParameter(scannedDataExporterClass, DataExporter.class);
                if (exportedClass == null) {
                    throw SeedException.createNew(DataErrorCode.MISSING_TYPE_PARAMETER).put("class", scannedDataExporterClass);
                }

                DataExporterDefinition dataExporterDefinition = new DataExporterDefinition(dataSet.name(), dataSet.group(), exportedClass, scannedDataExporterClass);
                nameDataExporterDefinitionMap.put(dataSet.name(), dataExporterDefinition);
                allDataExporters.put(dataSet.group(), nameDataExporterDefinitionMap);
            }
        }


        Collection<Class<?>> scannedDataImporterClasses = initContext.scannedTypesBySpecification().get(dataImporterSpecification);
        for (Class<?> scannedDataImporterClass : scannedDataImporterClasses) {
            if (DataImporter.class.isAssignableFrom(scannedDataImporterClass)) {
                DataSet dataSet = scannedDataImporterClass.getAnnotation(DataSet.class);
                Map<String, DataImporterDefinition<Object>> nameDataImporterDefinitionMap = allDataImporters.get(dataSet.group());
                if (nameDataImporterDefinitionMap == null) {
                    nameDataImporterDefinitionMap = new HashMap<String, DataImporterDefinition<Object>>();
                }

                Class actualType = getTypeParameter(scannedDataImporterClass, DataImporter.class);
                if (actualType == null) {
                    throw SeedException.createNew(DataErrorCode.MISSING_TYPE_PARAMETER).put("class", scannedDataImporterClass);
                }

                DataImporterDefinition dataImporterDefinition = new DataImporterDefinition(dataSet.name(), dataSet.group(), actualType, scannedDataImporterClass);
                nameDataImporterDefinitionMap.put(dataSet.name(), dataImporterDefinition);
                allDataImporters.put(dataSet.group(), nameDataImporterDefinitionMap);
            }
        }

        return InitState.INITIALIZED;
    }

    @Override
    public void start(Context context) {
        ClassLoader classLoader = SeedReflectionUtils.findMostCompleteClassLoader(DataPlugin.class);

        if (loadInitializationData) {
            for (Map<String, DataImporterDefinition<Object>> dataImporterDefinitionMap : allDataImporters.values()) {
                for (DataImporterDefinition<Object> dataImporterDefinition : dataImporterDefinitionMap.values()) {
                    String dataPath = String.format("META-INF/data/%s/%s.json", dataImporterDefinition.getGroup(), dataImporterDefinition.getName());
                    InputStream dataStream = classLoader.getResourceAsStream(dataPath);

                    if (dataStream != null) {
                        if (!dataManager.isInitialized(dataImporterDefinition.getGroup(), dataImporterDefinition.getName()) || forceInitializationData) {
                            LOGGER.info("Importing initialization data for {}.{}", dataImporterDefinition.getGroup(), dataImporterDefinition.getName());
                            dataManager.importData(dataStream, dataImporterDefinition.getGroup(), dataImporterDefinition.getName(), true);
                        }

                        try {
                            dataStream.close();
                        } catch (IOException e) {
                            LOGGER.warn("Unable to close data resource " + dataPath, e);
                        }
                    }
                }
            }
        }
    }

    private Class getTypeParameter(Class<?> scannedDataImporterClass, Class<?> genericInterface) {
        Class actualType = null;
        // Get all generic interfaces implemented by the scanned class
        Type[] genericInterfaces = scannedDataImporterClass.getGenericInterfaces();
        for (Type type : genericInterfaces) {
            if (type instanceof ParameterizedType) {
                Class anInterface = (Class) ((ParameterizedType) type).getRawType();
                // If the interface is DataImporter get its type parameter
                if (genericInterface.isAssignableFrom(anInterface)) {
                    actualType = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
                }
            }
        }
        return actualType;
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(ConfigurationProvider.class);
    }

    @Override
    public Object nativeUnitModule() {
        return new DataModule(allDataExporters, allDataImporters);
    }
}
