/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.hal;

import org.junit.Test;
import org.seedstack.seed.rest.api.hal.HalDefaultRepresentation;
import org.seedstack.seed.rest.internal.hal.fixture.OrderRepresentation;
import org.seedstack.seed.rest.internal.hal.fixture.OrdersRepresentation;
import org.seedstack.seed.rest.internal.hal.fixture.RepresentationFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class HalBuilderTest {

    @Test
    public void build_hal_representation() {
        HalDefaultRepresentation halRep = new RepresentationFactory().createOrders();

        // ---------------------------------------------------

        // Check orders
        assertThat(halRep.getResource()).isNotNull();
        assertThat(halRep.getResource()).isInstanceOf(OrdersRepresentation.class);
        assertThat(((OrdersRepresentation) halRep.getResource()).getCurrentlyProcessing()).isEqualTo(14);
        assertThat(((OrdersRepresentation) halRep.getResource()).getShippedToday()).isEqualTo(20);

        assertThat(halRep.getLinks()).hasSize(3);
        assertThat(halRep.getLinks().get("self").get(0).getHref()).isEqualTo("/orders");
        assertThat(halRep.getLinks().get("next").get(0).getHref()).isEqualTo("/orders?page=2");
        assertThat(halRep.getLinks().get("find").get(0).getHref()).isEqualTo("/orders{?id}");
        assertThat(halRep.getLinks().get("find").get(0).isTemplated()).isTrue();

        assertThat(halRep.getEmbedded()).isNotNull();
        assertThat((List) halRep.getEmbedded().get("orders")).hasSize(2);

        // check embedded 1
        HalDefaultRepresentation halRep1 = ((HalDefaultRepresentation) ((List) halRep.getEmbedded().get("orders")).get(0));
        assertThat(halRep1.getLinks()).hasSize(3);
        assertThat(halRep1.getLinks().get("self").get(0).getHref()).isEqualTo("/order/123");
        assertThat(halRep1.getLinks().get("basket").get(0).getHref()).isEqualTo("/baskets/98712");
        assertThat(halRep1.getLinks().get("customer").get(0).getHref()).isEqualTo("/customers/7809");

        OrderRepresentation order1 = (OrderRepresentation) halRep1.getResource();
        assertThat(order1.getTotal()).isEqualTo(30.00f);
        assertThat(order1.getCurrency()).isEqualTo("USD");
        assertThat(order1.getStatus()).isEqualTo("shipped");

        // check embedded 2
        HalDefaultRepresentation halRep2 = ((HalDefaultRepresentation) ((List) halRep.getEmbedded().get("orders")).get(1));
        assertThat(halRep2.getLinks()).hasSize(3);
        assertThat(halRep2.getLinks().get("self").get(0).getHref()).isEqualTo("/order/124");
        assertThat(halRep2.getLinks().get("basket").get(0).getHref()).isEqualTo("/baskets/97213");
        assertThat(halRep2.getLinks().get("customer").get(0).getHref()).isEqualTo("/customers/12369");

        OrderRepresentation order2 = (OrderRepresentation) halRep2.getResource();
        assertThat(order2.getTotal()).isEqualTo(20.00f);
        assertThat(order2.getCurrency()).isEqualTo("USD");
        assertThat(order2.getStatus()).isEqualTo("processing");
    }
}
