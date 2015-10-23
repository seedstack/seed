/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.api;

import java.util.Map;

/**
 * The diagnostic manager enables to access diagnostic information or write it somewhere.
 *
 * @author adrien.lauer@mpsa.com
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
     * Dump the diagnostic information through the {@link org.seedstack.seed.core.spi.diagnostic.DiagnosticReporter}.
     *
     * @param t the exception which may be the origin of this diagnostic generation. May be null.
     */
    void dumpDiagnosticReport(Throwable t);
}
