/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import org.seedstack.seed.diagnostic.DiagnosticManager;
import org.seedstack.seed.diagnostic.spi.DiagnosticInfoCollector;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("/seedstack/diagnostic")
public class DiagnosticResource {
    @Inject
    private DiagnosticManager diagnosticManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> diag() {
        return diagnosticManager.getDiagnosticInfo(null);
    }
}
