/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.fixtures;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.seedstack.seed.rest.Rel;
import org.seedstack.seed.rest.RelRegistry;
import org.seedstack.seed.rest.hal.HalBuilder;

@Path("orders")
@Rel(OrdersResource.ORDERS_REL)
public class OrdersResource {

    public static final String ORDER_REL = "order";
    public static final String ORDER_REL2 = "order2";
    public static final String ORDERS_REL = "orders";

    @Inject
    private RelRegistry relRegistry;

    @GET
    @Produces("application/hal+json")
    public Response getOrders(@QueryParam("page") int page) {
        return Response.ok(new RepresentationFactory(relRegistry).createOrders()).build();
    }

    @Rel(value = ORDER_REL, home = true)
    @GET
    @Path("{id}")
    @Produces("application/hal+json")
    public Response getOrder(@PathParam("id") String id) {
        return Response.ok(
                new OrderHal(relRegistry, id, "USD", "shipped", 10.20f)
                        .link(WarehousesResource.REL_WAREHOUSE,
                                relRegistry.uri(WarehousesResource.REL_WAREHOUSE).set("id", 56))
                        .link(InvoicesResource.REL_INVOICE,
                                relRegistry.uri(InvoicesResource.REL_INVOICE).set("id", 873)))
                .build();
    }

    @Rel(value = ORDER_REL2, home = true)
    @GET
    @Path("v2/{id}")
    @Produces("application/hal+json")
    public Response getOrder2(@PathParam("id") String id) {
        return Response.ok(
                HalBuilder.create(new OrderRepresentation(10.20f, "USD", "shipped"))
                        .self(relRegistry.uri(ORDER_REL2).set("id", id))
                        .link(WarehousesResource.REL_WAREHOUSE,
                                relRegistry.uri(WarehousesResource.REL_WAREHOUSE).set("id", 56))
                        .link(InvoicesResource.REL_INVOICE,
                                relRegistry.uri(InvoicesResource.REL_INVOICE).set("id", 873)))
                .build();
    }
}
