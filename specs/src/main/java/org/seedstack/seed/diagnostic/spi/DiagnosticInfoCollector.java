/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.diagnostic.spi;

import java.util.Map;

/**
 * Implement this interface to create a diagnostic information collector which will be used when a diagnostic
 * report is built.
 */
public interface DiagnosticInfoCollector {
    /**
     * Called when a diagnostic report is built.
     *
     * @return the diagnostic information indexed by name.
     */
    Map<String, Object> collect();
}
