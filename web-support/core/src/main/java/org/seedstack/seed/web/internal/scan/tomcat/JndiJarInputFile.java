/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.scan.tomcat;

import org.reflections.vfs.Vfs;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

/**
 * VFS file implementation for JNDI JAR scanning.
 *
 * @author adrien.lauer@mpsa.com
 */
class JndiJarInputFile implements Vfs.File {
    private final ZipEntry entry;
    private final JarInputStream jarInputStream;

    JndiJarInputFile(ZipEntry entry, JarInputStream jarInputStream) {
        this.entry = entry;
        this.jarInputStream = jarInputStream;
    }

    @Override
    public String getName() {
        String name = entry.getName();
        return name.substring(name.lastIndexOf('/') + 1);
    }

    @Override
    public String getRelativePath() {
        return entry.getName();
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return jarInputStream.read();
            }

            @Override
            public int read(byte[] b) throws IOException {
                return jarInputStream.read(b);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return jarInputStream.read(b, off, len);
            }

            @Override
            public long skip(long n) throws IOException {
                return jarInputStream.skip(n);
            }

            @Override
            public int available() throws IOException {
                return jarInputStream.available();
            }

            @Override
            public void close() throws IOException {
                jarInputStream.closeEntry();
            }
        };
    }
}
