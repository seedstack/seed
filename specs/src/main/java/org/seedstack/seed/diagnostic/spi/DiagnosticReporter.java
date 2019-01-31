/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.diagnostic.spi;

import java.util.Map;

/**
 * Implement this interface to create a diagnostic reporter which will provide persistent writing of diagnostic
 * information.
 */
public interface DiagnosticReporter {
    /**
     * Write the diagnostic information.
     *
     * @param diagnosticInfo the diagnostic information to write.
     */
    void writeDiagnosticReport(Map<String, Object> diagnosticInfo) throws Exception;
}
