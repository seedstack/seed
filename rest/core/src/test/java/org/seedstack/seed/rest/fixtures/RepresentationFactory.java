/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.fixtures;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.UriBuilder;
import org.seedstack.seed.rest.hal.HalBuilder;
import org.seedstack.seed.rest.hal.HalDefaultRepresentation;
import org.seedstack.seed.rest.hal.HalRepresentation;
import org.seedstack.seed.rest.hal.Link;

public class RepresentationFactory {

    public HalDefaultRepresentation createOrders() {

        OrdersRepresentation orders = new OrdersRepresentation(14, 20);

        List<HalRepresentation> embedded = new ArrayList<>();

        embedded.add(HalBuilder.create(new OrderRepresentation(30.00f, "USD", "shipped"))
                .self("/rest/order/123").link("basket", "/rest/baskets/98712").link("customer",
                        "/rest/customers/7809"));

        embedded.add(HalBuilder.create(new OrderRepresentation(20.00f, "USD", "processing"))
                .self("/rest/order/124").link("basket", "/rest/baskets/97213").link("customer",
                        "/rest/customers/12369"));

        return (HalDefaultRepresentation) HalBuilder.create(orders)
                .link("self", "/rest/orders")
                .link("next", UriBuilder.fromPath("/rest/orders").queryParam("page", 2).build().toString())
                .link("find", new Link("/rest/orders{?id}").templated())
                .embedded("orders", embedded);
    }
}
