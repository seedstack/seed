/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.fixtures;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.seedstack.seed.diagnostic.spi.DiagnosticDomain;
import org.seedstack.seed.diagnostic.spi.DiagnosticInfoCollector;

@DiagnosticDomain("it-collector")
public class TestDiagnosticCollector implements DiagnosticInfoCollector {
    @Inject
    Service1 service1;

    @Override
    public Map<String, Object> collect() {
        Map<String, Object> result = new HashMap<>();
        result.put("service", service1.toString());
        return result;
    }
}
