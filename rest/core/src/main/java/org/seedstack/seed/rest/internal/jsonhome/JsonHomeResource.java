/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.jsonhome;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Exposes the JSON-HOME resource on the application root path.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 * @see org.seedstack.seed.rest.internal.jsonhome.JsonHome
 */
@Path("/")
public class JsonHomeResource {

    private final JsonHome jsonHome;

    /**
     * Constructor.
     *
     * @param jsonHome the JSON-HOME resource
     */
    @Inject
    public JsonHomeResource(JsonHome jsonHome) {
        this.jsonHome = jsonHome;
    }

    /**
     * Exposes the JSON-HOME resource.
     * <p>
     * Returns an error 500 when a JsonProcessingException occurs.
     * </p>
     *
     * @return JAX-RS response
     */
    @GET
    @Produces({"application/json", "application/json-home"})
    public Response entryPoint() {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return Response.ok(mapper.writeValueAsString(jsonHome)).build();
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error processing JSON-HOME").build();
        }
    }
}
