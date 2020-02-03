/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.jsonhome;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.seedstack.seed.rest.spi.RootResource;

/**
 * Exposes the JSON-HOME resource on the application root path.
 *
 * @see org.seedstack.seed.rest.internal.jsonhome.JsonHome
 */
public class JsonHomeRootResource implements RootResource {
    private final JsonHome jsonHome;

    /**
     * Constructor.
     *
     * @param jsonHome the JSON-HOME resource
     */
    @Inject
    public JsonHomeRootResource(JsonHome jsonHome) {
        this.jsonHome = jsonHome;
    }

    @Override
    public Response buildResponse(HttpServletRequest httpServletRequest, UriInfo uriInfo) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return Response.ok(mapper.writeValueAsString(jsonHome)).type(new MediaType("application", "json")).build();
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error processing JSON-HOME").type(
                    MediaType.TEXT_PLAIN).build();
        }
    }
}
