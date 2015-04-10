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

import io.nuun.kernel.api.plugin.PluginException;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.persistence.jdbc.internal.JdbcPlugin;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

class SeedConfEntityManagerFactoryResolver {

    EntityManagerFactory resolve(String persistenceUnit, Map<String, String> properties, Configuration unitConfiguration, Application application, JdbcPlugin jdbcPlugin, Collection<Class<?>> scannedClasses) throws UnitNotConfiguredException {
        String dataSourceName = unitConfiguration.getString("datasource");
        String jtaDataSourceName = unitConfiguration.getString("jta-datasource");

        if (dataSourceName == null && jtaDataSourceName == null) {
            throw new UnitNotConfiguredException("No property datasource or jta-datasource found for persistenceUnit [" + persistenceUnit
                    + "]. One of these properties is required to load the persistence unit from seed configuration");
        }

        DataSource dataSource;
        boolean isJta = false;

        if (jtaDataSourceName != null) {
            dataSource = jdbcPlugin.getDataSources().get(jtaDataSourceName);
            isJta = true;
        } else {
            dataSource = jdbcPlugin.getDataSources().get(dataSourceName);
        }

        InternalPersistenceUnitInfo unitInfo = new InternalPersistenceUnitInfo();

        unitInfo.persistenceUnitName = persistenceUnit;
        if (isJta) {
            unitInfo.jtaDataSource = dataSource;
        } else {
            unitInfo.nonJtaDataSource = dataSource;
        }

        unitInfo.managedClassNames = new ArrayList<String>();
        for (Class<?> managed : scannedClasses) {
            if (persistenceUnit.equals(application.getConfiguration(managed).getString("jpa-unit"))) {
                unitInfo.managedClassNames.add(managed.getName());
            }
        }

        if (unitInfo.managedClassNames.isEmpty()) {
            throw new PluginException("No class was configured to belong to jpa unit [" + persistenceUnit + "]");
        }

        if (unitConfiguration.getString("mapping-files") != null) {
            unitInfo.mappingFileNames = Arrays.asList(unitConfiguration.getStringArray("mapping-files"));
        } else {
            unitInfo.mappingFileNames = Collections.emptyList();
        }

        unitInfo.properties = new Properties();
        unitInfo.properties.putAll(properties);

        if (unitConfiguration.getString("validation-mode") != null) {
            unitInfo.validationMode = ValidationMode.valueOf(unitConfiguration.getString("validation-mode"));
        }

        if (unitConfiguration.getString("shared-cache-mode") != null) {
            unitInfo.sharedCacheMode = SharedCacheMode.valueOf(unitConfiguration.getString("shared-cache-mode"));
        }

        if (unitConfiguration.getString("transaction-type") != null) {
            unitInfo.persistenceUnitTransactionType = PersistenceUnitTransactionType.valueOf(unitConfiguration.getString("transaction-type"));
        }

        return createEntityManagerFactory(unitInfo, properties);
    }

    // Method inspired by javax.persistence.Persistence.createEntityManagerFactory(String, Map)
    private EntityManagerFactory createEntityManagerFactory(InternalPersistenceUnitInfo info, Map<String, String> map) {
        EntityManagerFactory fac = null;
        List<PersistenceProvider> persistenceProviders = PersistenceProviderResolverHolder.getPersistenceProviderResolver().getPersistenceProviders();

        for (PersistenceProvider persistenceProvider : persistenceProviders) {
            info.persistenceProviderClassName = persistenceProvider.getClass().getName();
            fac = persistenceProvider.createContainerEntityManagerFactory(info, map);
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
