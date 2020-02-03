/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.scan.websphere;

import com.google.common.collect.AbstractIterator;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import org.reflections.vfs.Vfs;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.web.internal.WebErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VFS file implementation for WebSphere WSJAR scanning. Scan for directory.
 */
class WsInputDir implements Vfs.Dir {
    private static final Logger LOGGER = LoggerFactory.getLogger(WsInputDir.class);

    private final URL url;
    private String classesPath;
    private String warfile;
    private JarInputStream jarInputStream;

    WsInputDir(URL url) {
        this.url = url;
    }

    @Override
    public String getPath() {
        return url.getPath();
    }

    @Override
    public Iterable<Vfs.File> getFiles() {
        return () -> new AbstractIterator<Vfs.File>() {
            {
                try {
                    String path = url.toExternalForm();

                    final String warExtension = ".war!";
                    warfile = path.substring(0, path.indexOf(warExtension) + ".war".length());
                    classesPath = path.substring(path.indexOf(warExtension) + warExtension.length() + 1, path.length());
                    jarInputStream = new JarInputStream(new URL(warfile).openStream());

                } catch (IOException e) {
                    LOGGER.warn("Unable to open WAR at {}, ignoring it", url.toExternalForm(), e);
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
                            return new WsInputFile(classesPath, entry, jarInputStream);
                        }
                    } catch (IOException e) {
                        throw SeedException.wrap(e, WebErrorCode.UNABLE_TO_SCAN_WEBSPHERE_DIRECTORY).put("path",
                                classesPath)
                                .put("warname", warfile);
                    }
                }

            }
        };
    }

    @Override
    public void close() {
        try {
            jarInputStream.close();
        } catch (IOException e) {
            LOGGER.warn("Unable to close WAR at {}", url.toExternalForm(), e);
        }
    }

}
