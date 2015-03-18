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
 * Creation : 18 févr. 2015
 */
package org.seedstack.seed.persistence.jdbc.internal;

import org.aopalliance.intercept.MethodInvocation;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.persistence.jdbc.api.Jdbc;
import org.seedstack.seed.persistence.jdbc.api.JdbcExceptionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;
import org.seedstack.seed.transaction.spi.TransactionMetadataResolver;

/**
 * TransactionMetadataResolver for JDBC support. It reads the JDBC annotation to know which datasource should be use.
 */
public class JdbcTransactionMetadataResolver implements TransactionMetadataResolver {

    static String defaultJdbc;

    @Override
    public TransactionMetadata resolve(MethodInvocation methodInvocation, TransactionMetadata defaults) {
        Jdbc jdbc = SeedReflectionUtils.getMethodOrAncestorMetaAnnotatedWith(methodInvocation.getMethod(), Jdbc.class);

        if (jdbc != null || JdbcTransactionHandler.class.equals(defaults.getHandler())) {
            String resourceName = defaultJdbc;
            if (jdbc != null && !"".equals(jdbc.value())) {
                resourceName = jdbc.value();
            }
            TransactionMetadata result = new TransactionMetadata();
            result.setHandler(JdbcTransactionHandler.class);
            result.setExceptionHandler(JdbcExceptionHandler.class);
            result.setResource(resourceName);
            return result;
        }
        return null;
    }

}
