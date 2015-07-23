/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.hal.fixture;

import org.seedstack.seed.rest.api.hal.HalBuilder;
import org.seedstack.seed.rest.api.hal.HalDefaultRepresentation;
import org.seedstack.seed.rest.api.hal.HalRepresentation;
import org.seedstack.seed.rest.api.hal.Link;

import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class RepresentationFactory {

    public HalDefaultRepresentation createOrders() {

        OrdersRepresentation orders = new OrdersRepresentation(14, 20);

        List<HalRepresentation> embedded = new ArrayList<HalRepresentation>();

        embedded.add(HalBuilder.create(new OrderRepresentation(30.00f, "USD", "shipped"))
                .self("/order/123").link("basket", "/baskets/98712").link("customer", "/customers/7809"));

        embedded.add(HalBuilder.create(new OrderRepresentation(20.00f, "USD", "processing"))
                .self("/order/124").link("basket", "/baskets/97213").link("customer", "/customers/12369"));

        return (HalDefaultRepresentation) HalBuilder.create(orders)
                .link("self", "/orders")
                .link("next", UriBuilder.fromPath("/orders").queryParam("page", 2).build().toString())
                .link("find", new Link("/orders{?id}").templated())
                .embedded("orders", embedded);
    }
}
