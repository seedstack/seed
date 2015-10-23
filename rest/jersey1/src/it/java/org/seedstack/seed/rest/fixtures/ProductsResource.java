/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.fixtures;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;

@Path("/products")
public class ProductsResource {
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public Response post(ProductRepresentation productRepresentation, @Context UriInfo uriInfo) throws URISyntaxException {
        return Response.created(new URI(uriInfo.getBaseUri() + "/products/1")).entity(productRepresentation).build();
    }
}
