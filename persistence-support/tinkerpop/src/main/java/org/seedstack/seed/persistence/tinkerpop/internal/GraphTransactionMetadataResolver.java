/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.tinkerpop.internal;

import org.aopalliance.intercept.MethodInvocation;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.persistence.tinkerpop.api.Graph;
import org.seedstack.seed.persistence.tinkerpop.api.GraphExceptionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;
import org.seedstack.seed.transaction.spi.TransactionMetadataResolver;

/**
 * This {@link org.seedstack.seed.transaction.spi.TransactionMetadataResolver} resolves metadata for transactions marked
 * with {@link org.seedstack.seed.persistence.tinkerpop.api.Graph}.
 *
 * @author adrien.lauer@mpsa.com
 */
class GraphTransactionMetadataResolver implements TransactionMetadataResolver {
    static String defaultGraph;

    @Override
    public TransactionMetadata resolve(MethodInvocation methodInvocation, TransactionMetadata defaults) {
        Graph graph = SeedReflectionUtils.getMetaAnnotationFromAncestors(methodInvocation.getMethod(), Graph.class);

        if (graph != null || GraphTransactionHandler.class.equals(defaults.getHandler())) {
            TransactionMetadata result = new TransactionMetadata();
            result.setHandler(GraphTransactionHandler.class);
            result.setExceptionHandler(GraphExceptionHandler.class);
            if (graph == null) {
                result.setResource(defaultGraph);
            } else {
                result.setResource(graph.value());
            }
            return result;
        }

        return null;
    }
}