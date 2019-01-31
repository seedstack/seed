/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.scan.websphere;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import org.reflections.vfs.Vfs;
import org.seedstack.seed.web.internal.scan.JarEntryInputStream;

/**
 * VFS file implementation for WebSphere WSJAR scanning. Scan for directory.
 */
class WsInputFile implements Vfs.File {
    private final ZipEntry entry;
    private final String classesPath;
    private final JarInputStream jarInputStream;

    WsInputFile(String classesPath, ZipEntry entry, JarInputStream warfile) {
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
        return new JarEntryInputStream(jarInputStream);
    }

}
