/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.scan.tomcat;

import static org.seedstack.shed.reflect.ReflectUtils.getValue;
import static org.seedstack.shed.reflect.ReflectUtils.makeAccessible;

import com.google.common.collect.Lists;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;
import org.reflections.vfs.Vfs;
import org.reflections.vfs.ZipDir;
import org.seedstack.seed.core.internal.scan.ClasspathScanHandler;
import org.seedstack.shed.reflect.Classes;

/**
 * Provides classpath scan capabilities for Tomcat environment.
 */
public class TomcatClasspathScanHandler implements ClasspathScanHandler {
    private static Optional<Class<Object>> warURLConnectionClass = Classes
            .optional("org.apache.catalina.webresources.war.WarURLConnection");

    @Override
    public List<Vfs.UrlType> urlTypes() {
        return Lists.newArrayList(
                new TomcatWarFileUrlType(),
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

    private static class TomcatWarFileUrlType implements Vfs.UrlType {
        @Override
        public boolean matches(URL url) {
            return warURLConnectionClass.isPresent()
                    && "war".equals(url.getProtocol())
                    && !url.toExternalForm().contains(".jar");
        }

        @Override
        public Vfs.Dir createDir(final URL url) throws Exception {
            URLConnection urlConnection = url.openConnection();
            Class<?> theClass = warURLConnectionClass.get();
            if (urlConnection.getClass().equals(theClass)) {
                Object wrappedJarUrlConnection = getValue(
                        makeAccessible(
                                theClass.getDeclaredField("wrappedJarUrlConnection")
                        ),
                        urlConnection
                );
                if (wrappedJarUrlConnection instanceof JarURLConnection) {
                    return new ZipDir(((JarURLConnection) wrappedJarUrlConnection).getJarFile());
                }
            }
            return null;
        }
    }
}
