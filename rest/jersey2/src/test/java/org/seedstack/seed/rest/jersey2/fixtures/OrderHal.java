/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.fixtures;

import org.seedstack.seed.rest.RelRegistry;
import org.seedstack.seed.rest.hal.HalRepresentation;

public class OrderHal extends HalRepresentation {

    private String currency;
    private String status;
    private float total;

    OrderHal() {
    }

    public OrderHal(RelRegistry relRegistry, String id, String currency, String status, float total) {
        self(relRegistry.uri(OrdersResource.ORDER_REL).set("id", id));
        this.currency = currency;
        this.status = status;
        this.total = total;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }
}
