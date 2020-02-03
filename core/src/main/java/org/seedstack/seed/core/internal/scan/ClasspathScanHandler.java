/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.scan;

import java.util.List;
import org.reflections.vfs.Vfs;

/**
 * Implement this interface and register the implementation with the ServiceLoader API (META-INF/services) to extend
 * the classpath scanning abilities.
 */
public interface ClasspathScanHandler {
    /**
     * @return the list of supported {@link Vfs.UrlType}.
     */
    List<Vfs.UrlType> urlTypes();
}
