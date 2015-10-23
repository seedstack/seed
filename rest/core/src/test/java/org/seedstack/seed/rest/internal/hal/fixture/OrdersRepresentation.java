/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.hal.fixture;

/**
* @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
*/
public class OrdersRepresentation {
    private int currentlyProcessing;
    private int shippedToday;

    public OrdersRepresentation() {
    }

    public OrdersRepresentation(int currentlyProcessing, int shippedToday) {
        this.currentlyProcessing = currentlyProcessing;
        this.shippedToday = shippedToday;
    }

    public int getCurrentlyProcessing() {
        return currentlyProcessing;
    }

    public void setCurrentlyProcessing(int currentlyProcessing) {
        this.currentlyProcessing = currentlyProcessing;
    }

    public int getShippedToday() {
        return shippedToday;
    }

    public void setShippedToday(int shippedToday) {
        this.shippedToday = shippedToday;
    }
}
