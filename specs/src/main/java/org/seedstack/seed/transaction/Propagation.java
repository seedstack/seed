/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.transaction;

/**
 * Enumerates all possible propagations.
 */
public enum Propagation {
    /**
     * Support a current transaction, throw an exception if none exists.
     * Analogous to EJB transaction attribute of the same name.
     */
    MANDATORY,

    /**
     * Support a current transaction, create a new one if none exists.
     * Analogous to EJB transaction attribute of the same name.
     *
     * <p>This is the default setting of a transaction annotation.</p>
     */
    REQUIRED,

    /**
     * Create a new transaction, suspend the current transaction if one exists.
     * Analogous to EJB transaction attribute of the same name.
     */
    REQUIRES_NEW,

    /**
     * Support a current transaction, execute non-transactionally if none exists.
     * Analogous to EJB transaction attribute of the same name.
     */
    SUPPORTS,

    /**
     * Execute non-transactionally, suspend the current transaction if one exists.
     * Analogous to EJB transaction attribute of the same name.
     */
    NOT_SUPPORTED,

    /**
     * Execute non-transactionally, throw an exception if a transaction exists.
     * Analogous to EJB transaction attribute of the same name.
     */
    NEVER,

    /**
     * Execute within a nested transaction if a current transaction exists,
     * behave like PROPAGATION_REQUIRED else. There is no analogous feature in EJB.
     */
    NESTED
}
