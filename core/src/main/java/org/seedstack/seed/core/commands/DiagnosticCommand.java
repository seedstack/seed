/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.commands;

import org.seedstack.seed.core.api.DiagnosticManager;
import org.seedstack.seed.core.spi.command.Command;
import org.seedstack.seed.core.spi.command.CommandDefinition;

import javax.inject.Inject;
import java.util.Map;

/**
 * Command to dump the application diagnostic information.
 *
 * @author adrien.lauer@mpsa.com
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
