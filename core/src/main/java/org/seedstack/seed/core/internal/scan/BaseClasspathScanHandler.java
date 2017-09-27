/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.scan;

import com.google.common.collect.Lists;
import java.util.List;
import org.reflections.vfs.Vfs;

/**
 * Provides classpath scan capabilities for Java base environment.
 */
public class BaseClasspathScanHandler implements ClasspathScanHandler {
    @Override
    public List<Vfs.UrlType> urlTypes() {
        return Lists.newArrayList(
                Vfs.DefaultUrlTypes.jarFile,
                Vfs.DefaultUrlTypes.jarUrl,
                Vfs.DefaultUrlTypes.directory,
                Vfs.DefaultUrlTypes.jarInputStream
        );
    }
}
