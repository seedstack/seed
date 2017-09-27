/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal.hal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import org.seedstack.seed.rest.hal.HalDefaultRepresentation;
import org.seedstack.seed.rest.hal.Link;
import org.seedstack.seed.rest.internal.hal.fixture.OrderRepresentation;
import org.seedstack.seed.rest.internal.hal.fixture.OrdersRepresentation;
import org.seedstack.seed.rest.internal.hal.fixture.RepresentationFactory;

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
        assertThat(((Link) halRep.getLink("self")).getHref()).isEqualTo("/rest/orders");
        assertThat(((Link) halRep.getLink("next")).getHref()).isEqualTo("/rest/orders?page=2");
        assertThat(((Link) halRep.getLink("find")).getHref()).isEqualTo("/rest/orders{?id}");
        assertThat(((Link) halRep.getLink("find")).isTemplated()).isTrue();

        assertThat(halRep.getEmbedded()).isNotNull();
        assertThat((List) halRep.getEmbedded().get("orders")).hasSize(2);

        // check embedded 1
        HalDefaultRepresentation halRep1 = ((HalDefaultRepresentation) ((List) halRep.getEmbedded().get("orders")).get(
                0));
        assertThat(halRep1.getLinks()).hasSize(3);
        assertThat(((Link) halRep1.getLink("self")).getHref()).isEqualTo("/rest/order/123");
        assertThat(((Link) halRep1.getLink("basket")).getHref()).isEqualTo("/rest/baskets/98712");
        assertThat(((Link) halRep1.getLink("customer")).getHref()).isEqualTo("/rest/customers/7809");

        OrderRepresentation order1 = (OrderRepresentation) halRep1.getResource();
        assertThat(order1.getTotal()).isEqualTo(30.00f);
        assertThat(order1.getCurrency()).isEqualTo("USD");
        assertThat(order1.getStatus()).isEqualTo("shipped");

        // check embedded 2
        HalDefaultRepresentation halRep2 = ((HalDefaultRepresentation) ((List) halRep.getEmbedded().get("orders")).get(
                1));
        assertThat(halRep2.getLinks()).hasSize(3);
        assertThat(((Link) halRep2.getLink("self")).getHref()).isEqualTo("/rest/order/124");
        assertThat(((Link) halRep2.getLink("basket")).getHref()).isEqualTo("/rest/baskets/97213");
        assertThat(((Link) halRep2.getLink("customer")).getHref()).isEqualTo("/rest/customers/12369");

        OrderRepresentation order2 = (OrderRepresentation) halRep2.getResource();
        assertThat(order2.getTotal()).isEqualTo(20.00f);
        assertThat(order2.getCurrency()).isEqualTo("USD");
        assertThat(order2.getStatus()).isEqualTo("processing");
    }
}
