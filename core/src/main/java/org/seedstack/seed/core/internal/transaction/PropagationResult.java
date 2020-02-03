/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.transaction;

class PropagationResult {
    private final boolean newTransactionNeeded;
    private final boolean suspendCurrentTransaction;

    PropagationResult(boolean newTransactionNeeded) {
        this.newTransactionNeeded = newTransactionNeeded;
        this.suspendCurrentTransaction = false;
    }

    PropagationResult(boolean newTransactionNeeded, boolean suspendCurrentTransaction) {
        this.newTransactionNeeded = newTransactionNeeded;
        this.suspendCurrentTransaction = suspendCurrentTransaction;
    }

    boolean isNewTransactionNeeded() {
        return newTransactionNeeded;
    }

    boolean isSuspendCurrentTransaction() {
        return suspendCurrentTransaction;
    }

    @Override
    public String toString() {
        return "PropagationResult{" +
                "newTransactionNeeded=" + newTransactionNeeded +
                '}';
    }
}