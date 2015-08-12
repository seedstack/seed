/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.jms.internal;

import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.jms.api.JmsConnection;
import org.seedstack.seed.transaction.spi.TransactionMetadata;
import org.seedstack.seed.transaction.spi.TransactionMetadataResolver;
import org.aopalliance.intercept.MethodInvocation;

import javax.jms.MessageListener;

/**
 * This {@link org.seedstack.seed.transaction.spi.TransactionMetadataResolver} resolves metadata for transactions marked
 * with {@link org.seedstack.seed.jms.api.JmsConnection}.
 *
 * @author adrien.lauer@mpsa.com
 */
class JmsTransactionMetadataResolver implements TransactionMetadataResolver {
    @Override
    public TransactionMetadata resolve(MethodInvocation methodInvocation, TransactionMetadata defaults) {
        Class<?> declaringClass = methodInvocation.getMethod().getDeclaringClass();
        if (MessageListener.class.isAssignableFrom(declaringClass)) {
            TransactionMetadata transactionMetadata = new TransactionMetadata();
            transactionMetadata.setHandler(JmsListenerTransactionHandler.class);
            transactionMetadata.setResource(declaringClass.getCanonicalName());

            return transactionMetadata;
        } else {
            JmsConnection jmsConnection = SeedReflectionUtils.getMetaAnnotationFromAncestors(methodInvocation.getMethod(), JmsConnection.class);

            if (jmsConnection != null) {
                TransactionMetadata transactionMetadata = new TransactionMetadata();
                transactionMetadata.setHandler(JmsTransactionHandler.class);
                transactionMetadata.setResource(jmsConnection.value());

                return transactionMetadata;
            }
        }

        return null;
    }
}
