/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.scan;

import org.reflections.vfs.Vfs;

import java.util.List;

/**
 * Implement this interface and register the implementation with the ServiceLoader API (META-INF/services) to extend
 * the classpath scanning abilities.
 *
 * @author adrien.lauer@mpsa.com
 */
public interface ClasspathScanHandler {
    /**
     * @return the list of supported {@link Vfs.UrlType}.
     */
    List<Vfs.UrlType> urlTypes();
}
