/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.fixtures;

import org.seedstack.seed.rest.Rel;
import org.seedstack.seed.rest.RelRegistry;
import org.seedstack.seed.rest.hal.HalBuilder;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@Path("orders")
public class HalResource {

    public static final String ORDER_REL = "order";
    public static final String ORDER_REL2 = "order2";

    @Inject
    private RelRegistry relRegistry;

    @GET
    @Produces("application/hal+json")
    public Response getOrders() {
        return Response.ok(new RepresentationFactory().createOrders()).build();
    }

    @Rel(value = ORDER_REL, home = true)
    @GET
    @Path("{id}")
    @Produces("application/hal+json")
    public Response getOrders(@PathParam("id") String id) {
        return Response.ok(
                new OrderHal(id, "USD", "shipped", 10.20f)
                        .link("warehouse", "/warehouse/" + 56)
                        .link("invoice", "/invoices/873"))
                .build();
    }

    @Rel(value = ORDER_REL2, home = true)
    @GET
    @Path("v2/{id}")
    @Produces("application/hal+json")
    public Response getOrders2(@PathParam("id") String id) {
        return Response.ok(HalBuilder.create(new OrderRepresentation(10.20f, "USD", "shipped"))
                .self(relRegistry.uri(ORDER_REL2).set("id", id).expand())
                .link("warehouse", "/warehouse/56")
                .link("invoice", "/invoices/873"))
                .build();
    }
}
