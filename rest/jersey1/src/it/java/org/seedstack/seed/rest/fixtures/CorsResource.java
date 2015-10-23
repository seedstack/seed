/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.fixtures;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;

@Path("/cors")
public class CorsResource {
    @GET
    public Response get(@Context HttpServletRequest httpServletRequest) throws URISyntaxException {
        if ((Boolean) httpServletRequest.getAttribute("cors.isCorsRequest"))
            return Response.ok().build();
        else
            return Response.serverError().build();
    }

    @POST
    public Response post(@Context HttpServletRequest httpServletRequest) throws URISyntaxException {
        if ((Boolean) httpServletRequest.getAttribute("cors.isCorsRequest"))
            return Response.ok().build();
        else
            return Response.serverError().build();
    }

    @PUT
    public Response put(@Context HttpServletRequest httpServletRequest) throws URISyntaxException {
        if ((Boolean) httpServletRequest.getAttribute("cors.isCorsRequest"))
            return Response.ok().build();
        else
            return Response.serverError().build();
    }

    @DELETE
    public Response delete(@Context HttpServletRequest httpServletRequest) throws URISyntaxException {
        if ((Boolean) httpServletRequest.getAttribute("cors.isCorsRequest"))
            return Response.ok().build();
        else
            return Response.serverError().build();
    }

    @HEAD
    public Response head(@Context HttpServletRequest httpServletRequest) throws URISyntaxException {
        if ((Boolean) httpServletRequest.getAttribute("cors.isCorsRequest"))
            return Response.ok().build();
        else
            return Response.serverError().build();
    }

    @OPTIONS
    public Response options(@Context HttpServletRequest httpServletRequest) throws URISyntaxException {
        if ((Boolean) httpServletRequest.getAttribute("cors.isCorsRequest"))
            return Response.ok().build();
        else
            return Response.serverError().build();
    }
}
