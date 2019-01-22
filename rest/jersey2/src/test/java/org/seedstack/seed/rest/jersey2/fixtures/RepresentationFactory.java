/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.fixtures;

import java.util.ArrayList;
import java.util.List;
import org.seedstack.seed.rest.RelRegistry;
import org.seedstack.seed.rest.hal.HalBuilder;
import org.seedstack.seed.rest.hal.HalDefaultRepresentation;
import org.seedstack.seed.rest.hal.HalRepresentation;

public class RepresentationFactory {

    private final RelRegistry relRegistry;

    public RepresentationFactory(RelRegistry relRegistry) {
        this.relRegistry = relRegistry;
    }

    public HalDefaultRepresentation createOrders() {

        OrdersRepresentation orders = new OrdersRepresentation(14, 20);

        List<HalRepresentation> embedded = new ArrayList<>();

        embedded.add(HalBuilder.create(new OrderRepresentation(30.00f, "USD", "shipped"))
                .self(relRegistry.uri(OrdersResource.ORDER_REL).set("id", 123))
                .link(BasketsResource.REL_BASKET, relRegistry.uri(BasketsResource.REL_BASKET).set("id", "98712"))
                .link(CustomersResource.REL_CUSTOMER,
                        relRegistry.uri(CustomersResource.REL_CUSTOMER).set("id", "7809")));

        embedded.add(HalBuilder.create(new OrderRepresentation(20.00f, "USD", "processing"))
                .self(relRegistry.uri(OrdersResource.ORDER_REL).set("id", "124"))
                .link(BasketsResource.REL_BASKET, relRegistry.uri(BasketsResource.REL_BASKET).set("id", "97213"))
                .link(CustomersResource.REL_CUSTOMER,
                        relRegistry.uri(CustomersResource.REL_CUSTOMER).set("id", "12369")));

        return (HalDefaultRepresentation) HalBuilder.create(orders)
                .link("self", relRegistry.uri(OrdersResource.ORDERS_REL))
                .link("next", relRegistry.uri(OrdersResource.ORDERS_REL).set("page", "2"))
                .link("find", relRegistry.uri(OrdersResource.ORDER_REL).templated())
                .embedded("orders", embedded);
    }
}
