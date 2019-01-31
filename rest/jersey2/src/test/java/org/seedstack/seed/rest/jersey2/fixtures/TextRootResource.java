/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.fixtures;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.seedstack.seed.rest.spi.RootResource;

public class TextRootResource implements RootResource {
    @Override
    public Response buildResponse(HttpServletRequest httpServletRequest, UriInfo uriInfo) {
        return Response.ok("Hello World!").type(MediaType.TEXT_PLAIN_TYPE).build();
    }
}
