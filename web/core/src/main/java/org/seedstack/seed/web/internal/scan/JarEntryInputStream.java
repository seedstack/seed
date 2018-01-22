/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal.scan;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarInputStream;

/**
 * This input stream delegates reading to the underlying {@link JarInputStream} and when closed, closes the
 * corresponding
 * {@link java.util.zip.ZipEntry};
 */
public class JarEntryInputStream extends InputStream {
    private final JarInputStream jarInputStream;

    public JarEntryInputStream(JarInputStream jarInputStream) {
        this.jarInputStream = jarInputStream;
    }

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
}
