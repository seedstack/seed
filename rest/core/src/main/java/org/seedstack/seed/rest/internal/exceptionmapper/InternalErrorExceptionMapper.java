/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal.exceptionmapper;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.seedstack.seed.Application;
import org.seedstack.seed.diagnostic.DiagnosticManager;
import org.seedstack.seed.web.WebConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default exception mapper for an caught exception with no exception mapper associated.
 * It returns an HTTP status 500 (internal server error).
 */
@Provider
public class InternalErrorExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger logger = LoggerFactory.getLogger(InternalErrorExceptionMapper.class);
    private final WebConfig webConfig;
    @Inject
    private DiagnosticManager diagnosticManager;

    @Inject
    public InternalErrorExceptionMapper(Application application) {
        webConfig = application.getConfiguration().get(WebConfig.class);
    }

    @Override
    public Response toResponse(Exception exception) {
        logger.error(exception.getMessage(), exception);
        if (webConfig.isRequestDiagnosticEnabled()) {
            diagnosticManager.dumpDiagnosticReport(exception);
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error").build();
    }
}
