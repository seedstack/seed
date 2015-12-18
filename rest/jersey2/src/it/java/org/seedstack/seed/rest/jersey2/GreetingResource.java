/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@Path("/hello")
public class GreetingResource {

    @GET
    @Path("/{name}")
    public String sayHello(@PathParam("name") String name) {
        return "hello " + name;
    }

    @GET
    public String sayHelloWorld() {
        return sayHello("world");
    }
}
