/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.exceptionmapper;

import org.seedstack.seed.Application;
import org.seedstack.seed.rest.RestConfig;
import org.seedstack.seed.security.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Default {@link AuthenticationException} exception mapper which returns an HTTP status 401 (unauthorized).
 */
@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationExceptionMapper.class);
    private final RestConfig.ExceptionMappingConfig exceptionMappingConfig;

    @Inject
    public AuthenticationExceptionMapper(Application application) {
        this.exceptionMappingConfig = application.getConfiguration().get(RestConfig.ExceptionMappingConfig.class);
    }

    @Override
    public Response toResponse(AuthenticationException exception) {
        if (exceptionMappingConfig.isDetailedLog()) {
            LOGGER.debug(exception.toString());
        } else {
            LOGGER.debug(exception.getMessage());
        }
        String message = "Unauthorized";
        if (exceptionMappingConfig.isDetailedUserMessage()) {
            message += ": " + exception.getMessage();
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity(message).build();
    }
}
