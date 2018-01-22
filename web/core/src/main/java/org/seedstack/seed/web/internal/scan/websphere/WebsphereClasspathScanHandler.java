/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal.scan.websphere;

import com.google.common.collect.Lists;
import java.net.URL;
import java.util.List;
import org.reflections.vfs.Vfs;
import org.seedstack.seed.core.internal.scan.ClasspathScanHandler;

/**
 * Provides classpath scan capabilities for Websphere environment.
 */
public class WebsphereClasspathScanHandler implements ClasspathScanHandler {
    @Override
    public List<Vfs.UrlType> urlTypes() {
        return Lists.newArrayList(new WebSphereJarUrlType());
    }

    private static class WebSphereJarUrlType implements Vfs.UrlType {
        @Override
        public boolean matches(URL url) {
            return "wsjar".equals(url.getProtocol()) && !url.toExternalForm().contains(".jar");
        }

        @Override
        public Vfs.Dir createDir(final URL url) {
            return new WsInputDir(url);
        }
    }
}
