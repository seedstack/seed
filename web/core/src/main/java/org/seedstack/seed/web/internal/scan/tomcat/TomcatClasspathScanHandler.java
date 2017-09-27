/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal.scan.tomcat;

import com.google.common.collect.Lists;
import java.net.URL;
import java.util.List;
import org.reflections.vfs.Vfs;
import org.seedstack.seed.core.internal.scan.ClasspathScanHandler;

/**
 * Provides classpath scan capabilities for Tomcat environment.
 */
public class TomcatClasspathScanHandler implements ClasspathScanHandler {
    @Override
    public List<Vfs.UrlType> urlTypes() {
        return Lists.newArrayList(
                new TomcatJndiJarUrlType(),
                new TomcatJndiFileUrlType()
        );
    }

    private static class TomcatJndiJarUrlType implements Vfs.UrlType {
        @Override
        public boolean matches(URL url) {
            return "jndi".equals(url.getProtocol()) && url.toExternalForm().contains(".jar");
        }

        @Override
        public Vfs.Dir createDir(final URL url) {
            return new JndiJarInputDir(url);
        }
    }

    private static class TomcatJndiFileUrlType implements Vfs.UrlType {
        @Override
        public boolean matches(URL url) {
            return "jndi".equals(url.getProtocol()) && !url.toExternalForm().contains(".jar");
        }

        @Override
        public Vfs.Dir createDir(final URL url) {
            return new JndiInputDir(url);
        }
    }
}
