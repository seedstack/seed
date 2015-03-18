/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.spring.internal;

import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.spring.api.SpringTransactionManager;
import org.seedstack.seed.transaction.spi.TransactionMetadata;
import org.seedstack.seed.transaction.spi.TransactionMetadataResolver;
import org.aopalliance.intercept.MethodInvocation;

/**
 * This {@link org.seedstack.seed.transaction.spi.TransactionMetadataResolver} resolves metadata for transactions marked
 * with {@link org.seedstack.seed.spring.api.SpringTransactionManager}.
 *
 * @author adrien.lauer@mpsa.com
 */
public class SpringTransactionMetadataResolver implements TransactionMetadataResolver {
    @Override
    public TransactionMetadata resolve(MethodInvocation methodInvocation, TransactionMetadata defaults) {
        SpringTransactionManager springTransactionManager = SeedReflectionUtils.getMetaAnnotationFromAncestors(methodInvocation.getMethod(), SpringTransactionManager.class);

        if (springTransactionManager != null) {
            TransactionMetadata result = new TransactionMetadata();
            result.setHandler(SpringTransactionHandler.class);
            result.setResource(springTransactionManager.value());
            return result;
        }

        return null;
    }
}