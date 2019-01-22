/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.fixtures;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/stream")
public class StreamResource {
    @GET
    @Produces("application/json")
    public Stream<String> hello() {
        return Stream.of("Hello", "world");
    }

    @POST
    @Consumes("application/json")
    public String hello(Stream<String> words) {
        return String.join(" ", words.collect(Collectors.toList())) + "!";
    }
}
