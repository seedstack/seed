/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 22 juin 2015
 */
package org.seedstack.seed.web.internal.scan.websphere;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import org.reflections.vfs.Vfs;

/**
 * VFS file implementation for WebSphere WSJAR scanning. Scan for directory.
 *
 * @author thierry.bouvet@mpsa.com
 */
public class WsInputFile implements Vfs.File {

    private ZipEntry entry;
    private String classesPath;

    private JarInputStream jarInputStream;

    public WsInputFile(String classesPath, ZipEntry entry, JarInputStream warfile) {
        this.entry = entry;
        this.jarInputStream = warfile;
        this.classesPath = classesPath;
    }

    @Override
    public String getName() {
        String name = entry.getName();
        return name.substring(name.lastIndexOf('/') + 1);
    }

    @Override
    public String getRelativePath() {
        return entry.getName().replaceFirst(classesPath, "");
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
