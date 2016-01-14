/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.exceptionmapper;

import org.seedstack.seed.security.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Default {@link AuthorizationException} exception mapper which returns an HTTP status 403 (forbidden).
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@Provider
public class AuthorizationExceptionMapper implements ExceptionMapper<AuthorizationException> {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationExceptionMapper.class);

    @Override
    public Response toResponse(AuthorizationException exception) {
        logger.debug(exception.getMessage(), exception);
        return Response.status(Response.Status.FORBIDDEN).entity("Forbidden").build();
    }
}
