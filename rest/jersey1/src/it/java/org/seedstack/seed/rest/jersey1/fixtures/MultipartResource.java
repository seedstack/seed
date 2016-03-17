/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey1.fixtures;

import com.sun.jersey.multipart.FormDataMultiPart;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/multipart")
public class MultipartResource {
    @POST
    @Consumes("multipart/form-data")
    public Response multipart(FormDataMultiPart formDataMultiPart) {
        return Response.ok().build();
    }
}
