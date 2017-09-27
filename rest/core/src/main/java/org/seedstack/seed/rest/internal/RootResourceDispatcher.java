/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import org.seedstack.seed.rest.spi.RootResource;

@Path("/")
public class RootResourceDispatcher {
    @Inject
    Map<Variant, RootResource> rootResources;

    @GET
    public Response lookup(@Context Request request, @Context HttpServletRequest httpServletRequest,
            @Context UriInfo uriInfo) {
        ArrayList<Variant> variants = new ArrayList<>(rootResources.keySet());

        if (!variants.isEmpty()) {
            Variant v = request.selectVariant(variants);
            if (v != null) {
                return rootResources.get(v).buildResponse(httpServletRequest, uriInfo);
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
