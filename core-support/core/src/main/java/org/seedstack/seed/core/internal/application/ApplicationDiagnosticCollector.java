/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.application;

import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.spi.diagnostic.DiagnosticDomain;
import org.seedstack.seed.core.spi.diagnostic.DiagnosticInfoCollector;
import org.apache.commons.configuration.ConfigurationConverter;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * This diagnostic collector provides information about the application itself (configuration).
 *
 * @author adrien.lauer@mpsa.com
 */
@DiagnosticDomain("org.seedstack.seed.core.application")
class ApplicationDiagnosticCollector implements DiagnosticInfoCollector {
    @Inject
    Application application;

    @Override
    public Map<String, Object> collect() {
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("configuration", ConfigurationConverter.getMap(application.getConfiguration()));

        return result;
    }
}
