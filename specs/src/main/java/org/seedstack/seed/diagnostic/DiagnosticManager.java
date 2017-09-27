/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.diagnostic;

import java.util.Map;
import org.seedstack.seed.diagnostic.spi.DiagnosticInfoCollector;
import org.seedstack.seed.diagnostic.spi.DiagnosticReporter;

/**
 * The diagnostic manager enables to access diagnostic information or write it somewhere.
 */
public interface DiagnosticManager {
    /**
     * Retrieve the diagnostic information as a Map.
     *
     * @param t the exception which may be the origin of this diagnostic generation. May be null.
     * @return the diagnostic information.
     */
    Map<String, Object> getDiagnosticInfo(Throwable t);

    /**
     * Dump the diagnostic information through the {@link DiagnosticReporter}.
     *
     * @param t the exception which may be the origin of this diagnostic generation. May be null.
     */
    void dumpDiagnosticReport(Throwable t);

    /**
     * Register a diagnostic collector.
     *
     * @param domain                  the diagnostic collector domain which will be used root node in the diagnostic
     *                                tree.
     * @param diagnosticInfoCollector the diagnostic collector to register.
     */
    void registerDiagnosticInfoCollector(String domain, DiagnosticInfoCollector diagnosticInfoCollector);
}
