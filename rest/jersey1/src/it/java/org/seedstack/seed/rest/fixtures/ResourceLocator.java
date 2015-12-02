/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.fixtures;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/locator")
public class ResourceLocator {
    @Path("/sub/{id}")
    public Object locator(@PathParam("id") String id) {
        return new SubResource(id);
    }
}
