/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.transaction.spi;

/**
 * This interface provides a way to access the instance of a transacted class. As an example, a different JPA
 * EntityManager must be provided for each thread and each transaction inside the current thread. A transactional link
 * is meant to implement this behavior.
 *
 * @param <T> the class representing the transacted resource.
 */
public interface TransactionalLink<T> {

    /**
     * Retrieve the correct instance of the class representing the transacted resource.
     *
     * @return the T instance.
     */
    T get();

}
