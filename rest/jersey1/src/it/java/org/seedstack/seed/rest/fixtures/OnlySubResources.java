/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.fixtures;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

public class OnlySubResources {
    @GET
    @Path(value = "sub1")
    public Response sub1() {
        return Response.ok("sub1").build();
    }

    @GET
    @Path(value = "sub2")
    public Response sub2() {
        return Response.ok("sub2").build();
    }
}
