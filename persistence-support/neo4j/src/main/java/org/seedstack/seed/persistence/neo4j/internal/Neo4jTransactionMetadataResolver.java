/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.neo4j.internal;

import org.aopalliance.intercept.MethodInvocation;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.persistence.neo4j.api.Neo4jDb;
import org.seedstack.seed.persistence.neo4j.api.Neo4jExceptionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;
import org.seedstack.seed.transaction.spi.TransactionMetadataResolver;

/**
 * This {@link TransactionMetadataResolver} resolves metadata for transactions marked
 * with {@link org.seedstack.seed.persistence.neo4j.api.Neo4jDb}.
 *
 * @author adrien.lauer@mpsa.com
 */
class Neo4jTransactionMetadataResolver implements TransactionMetadataResolver {
    static String defaultDb;

    @Override
    public TransactionMetadata resolve(MethodInvocation methodInvocation, TransactionMetadata defaults) {
        Neo4jDb neo4jDb = SeedReflectionUtils.getMethodOrAncestorMetaAnnotatedWith(methodInvocation.getMethod(), Neo4jDb.class);

        if (neo4jDb != null || Neo4jTransactionHandler.class.equals(defaults.getHandler())) {
            TransactionMetadata result = new TransactionMetadata();
            result.setHandler(Neo4jTransactionHandler.class);
            result.setExceptionHandler(Neo4jExceptionHandler.class);
            result.setResource(neo4jDb == null ? defaultDb : neo4jDb.value());
            return result;
        }

        return null;
    }
}