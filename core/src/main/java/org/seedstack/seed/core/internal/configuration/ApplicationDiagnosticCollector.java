/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.configuration;

import java.util.HashMap;
import java.util.Map;
import org.seedstack.seed.Application;
import org.seedstack.seed.diagnostic.spi.DiagnosticInfoCollector;

class ApplicationDiagnosticCollector implements DiagnosticInfoCollector {
    private final Application application;

    ApplicationDiagnosticCollector(Application application) {
        this.application = application;
    }

    @Override
    public Map<String, Object> collect() {
        Map<String, Object> result = new HashMap<>();

        result.put("id", application.getId());
        result.put("name", application.getName());
        result.put("version", application.getVersion());
        if (application.isStorageEnabled()) {
            result.put("storage", application.getStorageLocation(""));
        }

        return result;
    }
}
