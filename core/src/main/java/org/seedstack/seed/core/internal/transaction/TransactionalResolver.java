/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.transaction;

import java.lang.reflect.Method;
import org.seedstack.seed.transaction.Transactional;
import org.seedstack.shed.reflect.StandardAnnotationResolver;

class TransactionalResolver extends StandardAnnotationResolver<Method, Transactional> {
    static TransactionalResolver INSTANCE = new TransactionalResolver();

    private TransactionalResolver() {
        // no external instantiation allowed
    }
}
