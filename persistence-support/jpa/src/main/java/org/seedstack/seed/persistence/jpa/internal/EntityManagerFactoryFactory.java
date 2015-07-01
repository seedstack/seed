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
 * Creation : 19 mars 2015
 */
package org.seedstack.seed.persistence.jpa.internal;

import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.persistence.jdbc.internal.JdbcRegistry;

import javax.persistence.*;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.util.*;

class EntityManagerFactoryFactory {
    EntityManagerFactory createEntityManagerFactory(String persistenceUnit, Properties properties) {
        return Persistence.createEntityManagerFactory(persistenceUnit, properties);
    }

    EntityManagerFactory createEntityManagerFactory(String persistenceUnit, Properties properties, Configuration unitConfiguration, Application application, JdbcRegistry jdbcRegistry, Collection<Class<?>> scannedClasses) {
        InternalPersistenceUnitInfo unitInfo = new InternalPersistenceUnitInfo(persistenceUnit);

        String dataSourceName = unitConfiguration.getString("datasource");
        DataSource dataSource = jdbcRegistry.getDataSource(dataSourceName);
        if (dataSource == null) {
            throw SeedException.createNew(JpaErrorCode.DATA_SOURCE_NOT_FOUND).put("unit", unitInfo.getPersistenceUnitName()).put("datasource", dataSourceName);
        }

        ArrayList<String> classNames = new ArrayList<String>();
        for (Class<?> scannedClass : scannedClasses) {
            if (unitInfo.getPersistenceUnitName().equals(application.getConfiguration(scannedClass).getString("jpa-unit"))) {
                classNames.add(scannedClass.getName());
            }
        }
        if (classNames.isEmpty()) {
            throw SeedException.createNew(JpaErrorCode.NO_PERSISTED_CLASSES_IN_UNIT).put("unit", unitInfo.getPersistenceUnitName());
        }
        unitInfo.setManagedClassNames(classNames);


        if (unitConfiguration.getString("mapping-files") != null) {
            unitInfo.setMappingFileNames(Arrays.asList(unitConfiguration.getStringArray("mapping-files")));
        } else {
            unitInfo.setMappingFileNames(Collections.<String>emptyList());
        }

        unitInfo.setProperties(properties);

        if (unitConfiguration.getString("validation-mode") != null) {
            unitInfo.setValidationMode(ValidationMode.valueOf(unitConfiguration.getString("validation-mode")));
        }

        if (unitConfiguration.getString("shared-cache-mode") != null) {
            unitInfo.setSharedCacheMode(SharedCacheMode.valueOf(unitConfiguration.getString("shared-cache-mode")));
        }

        if (unitConfiguration.getString("transaction-type") != null) {
            unitInfo.setPersistenceUnitTransactionType(PersistenceUnitTransactionType.valueOf(unitConfiguration.getString("transaction-type")));
        }

        switch (unitInfo.getTransactionType()) {
            case RESOURCE_LOCAL:
                unitInfo.setNonJtaDataSource(dataSource);
                break;
            case JTA:
                unitInfo.setJtaDataSource(dataSource);
                break;
        }

        return createEntityManagerFactory(unitInfo, null);
    }

    // Method inspired by javax.persistence.Persistence.createEntityManagerFactory(String, Map)
    private EntityManagerFactory createEntityManagerFactory(InternalPersistenceUnitInfo info, Properties properties) {
        HashMap<String, String> propertiesMap = new HashMap<String, String>();
        if (properties != null) {
            for (Object key : properties.keySet()) {
                propertiesMap.put((String) key, properties.getProperty((String) key));
            }
        }

        EntityManagerFactory fac = null;
        List<PersistenceProvider> persistenceProviders = PersistenceProviderResolverHolder.getPersistenceProviderResolver().getPersistenceProviders();

        for (PersistenceProvider persistenceProvider : persistenceProviders) {
            info.setPersistenceProviderClassName(persistenceProvider.getClass().getName());
            fac = persistenceProvider.createContainerEntityManagerFactory(info, propertiesMap);
            if (fac != null) {
                break;
            }
        }

        if (fac == null) {
            throw new PersistenceException("No Persistence provider for EntityManager named " + info.getPersistenceUnitName());
        }

        return fac;
    }

}
