/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.exceptionmapper;

import org.seedstack.seed.Application;
import org.seedstack.seed.DiagnosticManager;
import org.seedstack.seed.web.internal.WebPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Default exception mapper for an caught exception with no exception mapper associated.
 * It returns an HTTP status 500 (internal server error).
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@Provider
public class InternalErrorExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger logger = LoggerFactory.getLogger(InternalErrorExceptionMapper.class);
    private static final String REQUEST_DIAGNOSTIC_ENABLE = WebPlugin.WEB_PLUGIN_PREFIX + ".request-diagnostic.enabled";

    @Inject
    private DiagnosticManager diagnosticManager;

    @Inject
    private Application application;

    @Override
    public Response toResponse(Exception exception) {
        logger.error(exception.getMessage(), exception);
        if (application.getConfiguration().getBoolean(REQUEST_DIAGNOSTIC_ENABLE, false)) {
            diagnosticManager.dumpDiagnosticReport(exception);
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error").build();
    }
}
