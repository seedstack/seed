/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.scan.tomcat;

import com.google.common.collect.Lists;
import org.reflections.vfs.Vfs;
import org.seedstack.seed.core.internal.scan.ClasspathScanHandler;

import java.net.URL;
import java.util.List;

/**
 * Provides classpath scan capabilities for Tomcat environment.
 *
 * @author adrien.lauer@mpsa.com
 */
public class TomcatClasspathScanHandler implements ClasspathScanHandler {
    @Override
    public List<Vfs.UrlType> urlTypes() {
        return Lists.newArrayList(
                new Vfs.UrlType() {
                    @Override
                    public boolean matches(URL url) {
                        return "jndi".equals(url.getProtocol()) && url.toExternalForm().contains(".jar");
                    }

                    @Override
                    public Vfs.Dir createDir(final URL url) {
                        return new JndiJarInputDir(url);
                    }
                },

                new Vfs.UrlType() {
                    @Override
                    public boolean matches(URL url) {
                        return "jndi".equals(url.getProtocol()) && !url.toExternalForm().contains(".jar");
                    }

                    @Override
                    public Vfs.Dir createDir(final URL url) {
                        return new JndiInputDir(url);
                    }
                }
        );
    }
}
