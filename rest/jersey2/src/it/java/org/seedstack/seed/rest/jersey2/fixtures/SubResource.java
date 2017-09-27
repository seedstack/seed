/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.jersey2.fixtures;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

public class SubResource {
    private final String id;

    public SubResource(String id) {
        this.id = id;
    }

    @GET
    public Response test() {
        return Response.ok("sub:" + id).build();
    }
}
