/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.scan.websphere;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.reflections.vfs.Vfs;
import org.seedstack.seed.core.internal.scan.ClasspathScanHandler;

/**
 * Provides classpath scan capabilities for Websphere environment.
 *
 * @author thierry.bouvet@mpsa.com
 */
public class WebsphereClasspathScanHandler implements ClasspathScanHandler {
    @Override
    public List<Vfs.UrlType> urlTypes() {
        Vfs.UrlType type = new Vfs.UrlType() {
            @Override
            public boolean matches(URL url) {
                return "wsjar".equals(url.getProtocol()) && !url.toExternalForm().contains(".jar");
            }

            @Override
            public Vfs.Dir createDir(final URL url) {
                return new WsInputDir(url);
            }
        };
        return Arrays.asList(type);
    }
}
