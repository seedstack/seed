/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.scan.tomcat;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import org.reflections.vfs.Vfs;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CorePlugin;
import org.seedstack.seed.web.internal.WebErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.AbstractIterator;

/**
 * VFS directory implementation for JNDI JAR scanning.
 *
 * @author adrien.lauer@mpsa.com
 */
class JndiJarInputDir implements Vfs.Dir {
    private static final Logger LOGGER = LoggerFactory.getLogger(JndiJarInputDir.class);

    private final URL url;

    JarInputStream jarInputStream;

    JndiJarInputDir(URL url) {
        this.url = url;
    }

    @Override
    public String getPath() {
        return url.getPath();
    }

    @Override
    public Iterable<Vfs.File> getFiles() {
        return new Iterable<Vfs.File>() {
            @Override
            public Iterator<Vfs.File> iterator() {
                return new AbstractIterator<Vfs.File>() {
                    {
                        try {
                            jarInputStream = new JarInputStream(url.openConnection().getInputStream());
                        } catch (Exception e) {
                            LOGGER.warn("Unable to open JAR at " + url.toExternalForm() + ", ignoring it");
                            LOGGER.debug(CorePlugin.DETAILS_MESSAGE, e);
                        }
                    }

                    @Override
                    protected Vfs.File computeNext() {
                        if (jarInputStream == null) {
                            return endOfData();
                        }

                        while (true) {
                            try {
                                ZipEntry entry = jarInputStream.getNextEntry();
                                if (entry == null) {
                                    return endOfData();
                                }

                                if (!entry.isDirectory()) {
                                    return new JndiJarInputFile(entry, jarInputStream);
                                }
                            } catch (IOException e) {
                                throw SeedException.wrap(e, WebErrorCode.UNABLE_TO_SCAN_TOMCAT_JNDI_JAR);
                            }
                        }
                    }
                };
            }
        };
    }

    @Override
    public void close() {
        try {
            jarInputStream.close();
        } catch (IOException e) {
            LOGGER.warn("Unable to close JAR at " + url.toExternalForm());
            LOGGER.debug(CorePlugin.DETAILS_MESSAGE, e);
        }
    }

}
