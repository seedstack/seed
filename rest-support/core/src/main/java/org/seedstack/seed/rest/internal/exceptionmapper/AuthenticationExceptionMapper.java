/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.exceptionmapper;

import org.seedstack.seed.security.api.exceptions.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Default {@link AuthenticationException} exception mapper which returns an HTTP status 401 (unauthorized).
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationExceptionMapper.class);

    @Override
    public Response toResponse(AuthenticationException exception) {
        logger.debug(exception.getMessage(), exception);
        return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();
    }
}
