/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell.internal;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class CRLFOutputStream extends FilterOutputStream {
    private int lastb = -1;
    private static byte[] newline;

    static {
        newline = new byte[2];
        newline[0] = (byte) '\r';
        newline[1] = (byte) '\n';
    }

    CRLFOutputStream(OutputStream os) {
        super(os);
    }

    @Override
    public void write(int b) throws IOException {
        if (b == '\r') {
            out.write(newline);
        } else if (b == '\n') {
            if (lastb != '\r') {
                out.write(newline);
            }
        } else {
            out.write(b);
        }
        lastb = b;
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        int start = off;

        int internalLen = len + off;
        for (int i = start; i < internalLen; i++) {
            if (b[i] == '\r') {
                out.write(b, start, i - start);
                out.write(newline);
                start = i + 1;
            } else if (b[i] == '\n') {
                if (lastb != '\r') {
                    out.write(b, start, i - start);
                    out.write(newline);
                }
                start = i + 1;
            }
            lastb = b[i];
        }
        if ((internalLen - start) > 0) {
            out.write(b, start, internalLen - start);
        }
    }
}