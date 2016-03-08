/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.scan.websphere;

import com.google.common.collect.AbstractIterator;
import org.reflections.vfs.Vfs;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.utils.SeedLoggingUtils;
import org.seedstack.seed.web.internal.WebErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

/**
 * VFS file implementation for WebSphere WSJAR scanning. Scan for directory.
 *
 * @author thierry.bouvet@mpsa.com
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
        return new Iterable<Vfs.File>() {
            @Override
            public Iterator<Vfs.File> iterator() {
                return new AbstractIterator<Vfs.File>() {
                    {
                        try {
                            String path = url.toExternalForm();

                            final String warExtension = ".war!";
                            warfile = path.substring(0, path.indexOf(warExtension) + ".war".length());
                            classesPath = path.substring(path.indexOf(warExtension) + warExtension.length() + 1, path.length());
                            jarInputStream = new JarInputStream(new URL(warfile).openStream());

                        } catch (Exception e) {
                            SeedLoggingUtils.logWarningWithDebugDetails(LOGGER, e, "Unable to open WAR at {}, ignoring it", url.toExternalForm());
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
                                throw SeedException.wrap(e, WebErrorCode.UNABLE_TO_SCAN_WEBSPHERE_DIRECTORY).put("path", classesPath)
                                        .put("warname", warfile);
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
            SeedLoggingUtils.logWarningWithDebugDetails(LOGGER, e, "Unable to close WAR at {}", url.toExternalForm());
        }
    }

}
