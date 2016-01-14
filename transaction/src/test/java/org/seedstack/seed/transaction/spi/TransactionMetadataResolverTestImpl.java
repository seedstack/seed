/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.transaction.spi;

import org.aopalliance.intercept.MethodInvocation;

public class TransactionMetadataResolverTestImpl implements TransactionMetadataResolver {

    @Override
    public TransactionMetadata resolve(MethodInvocation methodInvocation, TransactionMetadata defaults) {
        TransactionMetadata transactionMetadata = new TransactionMetadata();
        transactionMetadata.setHandler(TransactionHandlerTestImpl.class);
        return transactionMetadata;
    }


}
