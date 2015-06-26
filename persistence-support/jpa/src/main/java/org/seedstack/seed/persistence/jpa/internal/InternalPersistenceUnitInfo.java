/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 12 mars 2015
 */
package org.seedstack.seed.persistence.jpa.internal;

import org.seedstack.seed.core.utils.SeedReflectionUtils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

class InternalPersistenceUnitInfo implements PersistenceUnitInfo {
    private final String persistenceUnitName;

    private String persistenceProviderClassName;

    private PersistenceUnitTransactionType persistenceUnitTransactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;

    private DataSource jtaDataSource;

    private DataSource nonJtaDataSource;

    private List<String> mappingFileNames;

    private List<String> managedClassNames;

    private SharedCacheMode sharedCacheMode = SharedCacheMode.UNSPECIFIED;

    private ValidationMode validationMode = ValidationMode.AUTO;

    private Properties properties;

    public InternalPersistenceUnitInfo(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    void setPersistenceProviderClassName(String persistenceProviderClassName) {
        this.persistenceProviderClassName = persistenceProviderClassName;
    }

    void setPersistenceUnitTransactionType(PersistenceUnitTransactionType persistenceUnitTransactionType) {
        this.persistenceUnitTransactionType = persistenceUnitTransactionType;
    }

    void setJtaDataSource(DataSource jtaDataSource) {
        this.jtaDataSource = jtaDataSource;
    }

    void setNonJtaDataSource(DataSource nonJtaDataSource) {
        this.nonJtaDataSource = nonJtaDataSource;
    }

    void setMappingFileNames(List<String> mappingFileNames) {
        this.mappingFileNames = mappingFileNames;
    }

    void setManagedClassNames(List<String> managedClassNames) {
        this.managedClassNames = managedClassNames;
    }

    void setSharedCacheMode(SharedCacheMode sharedCacheMode) {
        this.sharedCacheMode = sharedCacheMode;
    }

    void setValidationMode(ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

    void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    @Override
    public String getPersistenceProviderClassName() {
        return persistenceProviderClassName;
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return persistenceUnitTransactionType;
    }

    @Override
    public DataSource getJtaDataSource() {
        return jtaDataSource;
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    @Override
    public List<String> getMappingFileNames() {
        return mappingFileNames;
    }

    @Override
    public List<URL> getJarFileUrls() {
        // Not used as Seed will scan the classes
        return Collections.emptyList();
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        // Not used as Seed will scan the classes
        return null;
    }

    @Override
    public List<String> getManagedClassNames() {
        return managedClassNames;
    }

    @Override
    public boolean excludeUnlistedClasses() {
        // Not used as Seed will scan the classes
        return false;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return sharedCacheMode;
    }

    @Override
    public ValidationMode getValidationMode() {
        return validationMode;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return SeedReflectionUtils.findMostCompleteClassLoader(InternalPersistenceUnitInfo.class);
    }

    @Override
    public void addTransformer(ClassTransformer transformer) {
        throw new UnsupportedOperationException("class transformation is not supported by managed JPA units");
    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        ClassLoader classLoader = getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            // this forks the application class loader into a new one with the same scope
            return new URLClassLoader(((URLClassLoader)classLoader).getURLs(), classLoader.getParent());
        } else {
            return classLoader;
        }
    }

}
