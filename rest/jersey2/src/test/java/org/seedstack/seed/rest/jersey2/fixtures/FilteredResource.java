/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.jersey2.fixtures;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/filter/message")
public class FilteredResource {

    public final static String OVERRIDDEN_ATTRIBUTE_NAME = "overridden";
    @Context
    private HttpServletRequest httpRequest;
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Message say(Message message) {
        String override = (String) httpRequest.getAttribute(OVERRIDDEN_ATTRIBUTE_NAME);
        return new Message(message.getAuthor() + " says: " + message.getBody(), override);
    }
}
