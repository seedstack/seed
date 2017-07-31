/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import com.google.inject.util.Types;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.seed.Application;
import org.seedstack.seed.ApplicationConfig;
import org.seedstack.seed.ClassConfiguration;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Implementation of the {@link Application} interface.
 */
class ApplicationImpl implements Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationImpl.class);
    private static final String CLASSES_CONFIGURATION_PREFIX = "classes";
    private final Coffig coffig;
    private final File storageDirectory;
    private final ApplicationConfig applicationConfig;

    ApplicationImpl(Coffig coffig, ApplicationConfig applicationConfig) {
        this.coffig = coffig;
        this.applicationConfig = applicationConfig;
        this.storageDirectory = configureLocalStorage(applicationConfig);
    }

    @Override
    public String getName() {
        return applicationConfig.getName();
    }

    @Override
    public String getId() {
        return applicationConfig.getId();
    }

    @Override
    public String getVersion() {
        return applicationConfig.getVersion();
    }

    @Override
    public File getStorageLocation(String context) {
        if (storageDirectory == null) {
            throw SeedException.createNew(CoreErrorCode.NO_LOCAL_STORAGE_CONFIGURED).put("context", context);
        }

        File location = new File(storageDirectory, context);
        ensureWritableDirectory(location);
        return location;
    }

    @Override
    public boolean isStorageEnabled() {
        return storageDirectory != null;
    }

    @Override
    public Coffig getConfiguration() {
        return coffig;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ClassConfiguration<T> getConfiguration(Class<T> aClass) {
        ClassConfiguration<T> classConfiguration = ClassConfiguration.empty(aClass);
        StringBuilder sb = new StringBuilder(CLASSES_CONFIGURATION_PREFIX);
        for (String part : aClass.getName().split("\\.")) {
            sb.append(".").append(part);
            coffig.getOptional(Types.newParameterizedType(ClassConfiguration.class, aClass), sb.toString())
                    .ifPresent(o -> classConfiguration.merge((ClassConfiguration<T>) o));
        }
        return classConfiguration;
    }

    @Override
    public String substituteWithConfiguration(String value) {
        return (String) coffig.getMapper().map(new ValueNode(value), String.class);
    }

    private File configureLocalStorage(ApplicationConfig applicationConfig) {
        File storageDirectory;
        if (applicationConfig.isStorageEnabled()) {
            storageDirectory = applicationConfig.getStorage();
            ensureWritableDirectory(storageDirectory);
            LOGGER.info("Application local storage at {}", storageDirectory.getAbsolutePath());
        } else {
            storageDirectory = null;
        }
        return storageDirectory;
    }

    private void ensureWritableDirectory(File location) {
        if (!location.exists() && !location.mkdirs()) {
            throw SeedException.createNew(CoreErrorCode.UNABLE_TO_CREATE_STORAGE_DIRECTORY).put("path", location.getAbsolutePath());
        }

        if (!location.isDirectory()) {
            throw SeedException.createNew(CoreErrorCode.STORAGE_PATH_IS_NOT_A_DIRECTORY).put("path", location.getAbsolutePath());
        }

        if (!location.canWrite()) {
            throw SeedException.createNew(CoreErrorCode.STORAGE_DIRECTORY_IS_NOT_WRITABLE).put("path", location.getAbsolutePath());
        }
    }
}
