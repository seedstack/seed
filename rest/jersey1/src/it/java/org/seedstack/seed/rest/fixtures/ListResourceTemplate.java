/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.fixtures;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;

public abstract class ListResourceTemplate<R> {
    @Inject
    List<R> theList;

    @GET
    public List<R> list() {
        return theList;
    }

    @GET
    @Path("/{index}")
    public R getByIndex(@PathParam("index") int index) {
        return theList.get(index);
    }
}
