/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.spi.diagnostic;

import java.util.Map;

/**
 * Implement this interface to create a diagnostic reporter which will provide persistent writing of diagnostic
 * information.
 *
 * @author adrien.lauer@mpsa.com
 */
public interface DiagnosticReporter {
    /**
     * Write the diagnostic information.
     * @param diagnosticInfo the diagnostic information to write.
     */
    void writeDiagnosticReport(Map<String, Object> diagnosticInfo) throws Exception;
}
