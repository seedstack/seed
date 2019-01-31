/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.fixtures;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path("/validating")
public class ValidatingResource {
    @Path("/body")
    @POST
    public Response createSomething(@Valid Something something) {
        return Response.created(UriBuilder.fromResource(ValidatingResource.class).build()).build();
    }

    @Path("/response")
    @GET
    @Valid
    public Something getSomething() {
        return new Something();
    }

    @Path("/queryparam")
    @GET
    @Valid
    public Something getSomething(@NotBlank(message = "someI18nKey") @QueryParam("param") String param) {
        return new Something();
    }

    @Path("/unknown")
    @GET
    public Something getValidatedSomething() {
        return validate(new Something());
    }

    Something validate(@Valid Something something) {
        return something;
    }
}
