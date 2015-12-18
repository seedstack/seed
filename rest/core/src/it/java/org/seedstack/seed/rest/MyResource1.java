/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest;

import org.seedstack.seed.Logging;
import org.slf4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * @author Pierre THIROUIN (pierre.thirouin@ext.inetpsa.com)
 */
@Path("/my-resource1")
public class MyResource1 {

    @Logging
    private Logger logger;

    @GET
    public String sayHello() {
        return "hello world!";
    }

    public Logger getLogger() {
        return logger;
    }
}
