/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal.scan.tomcat;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import org.reflections.vfs.Vfs;
import org.seedstack.seed.web.internal.scan.JarEntryInputStream;

/**
 * VFS file implementation for JNDI JAR scanning.
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
        return new JarEntryInputStream(jarInputStream);
    }
}
