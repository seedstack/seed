/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.diagnostic;

import java.util.Map;
import javax.inject.Inject;
import org.seedstack.seed.command.Command;
import org.seedstack.seed.command.CommandDefinition;
import org.seedstack.seed.diagnostic.DiagnosticManager;

/**
 * Command to dump the application diagnostic information.
 */
@CommandDefinition(scope = "core", name = "diag", description = "Build a diagnostic report")
public class DiagnosticCommand implements Command<Map<String, Object>> {
    @Inject
    DiagnosticManager diagnosticManager;

    @Override
    public Map<String, Object> execute(Object object) throws Exception {
        return diagnosticManager.getDiagnosticInfo(null);
    }
}
