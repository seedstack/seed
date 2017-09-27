/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal.scan.websphere;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Verifications;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.reflections.vfs.Vfs;
import org.seedstack.seed.SeedException;

public class WsInputDirTest {

    @Test
    public void test(@SuppressWarnings("unused") @Mocked final WsInputFile wsInputFile) throws Exception {
        final String warfile = "C/test.war";
        final String classesPath = "WEB-INF/classes";

        final MockUp<ZipEntry> zipEntry = new MockUp<ZipEntry>() {
        };

        new MockUp<JarInputStream>() {
            final int maxInvocations = 2;
            int invocations;

            @Mock
            public ZipEntry getNextEntry() {
                invocations++;
                if (invocations <= maxInvocations)
                    return zipEntry.getMockInstance();
                return null;
            }
        };

        final MockUp<InputStream> inputStream = new MockUp<InputStream>() {
        };

        URLStreamHandler goodStubUrlHandler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return null;
            }
        };

        final URL goodURL = new URL("wsjar", "", -1, "file:" + warfile + "!/" + classesPath, goodStubUrlHandler);
        final URL warURL = new URL("wsjar", "", -1, "file:" + warfile, goodStubUrlHandler);

        new Expectations(URL.class) {
            {
                new URL("wsjar:file:" + warfile);
                result = warURL;

                Deencapsulation.invoke(warURL, "openStream");
                returns(inputStream.getMockInstance());
            }
        };
        new Expectations(zipEntry) {
            {
                zipEntry.getMockInstance().isDirectory();
                returns(true, false);
            }
        };

        WsInputDir jarInputDir = new WsInputDir(goodURL);
        jarInputDir.getFiles();

        for (@SuppressWarnings("unused")
                Vfs.File file : jarInputDir.getFiles()) {
            // nothing to check
        }

        new Verifications() {
            {
                new WsInputFile(classesPath, zipEntry.getMockInstance(), (JarInputStream) any);
                times = 1;
            }
        };

        Assertions.assertThat(jarInputDir.getPath()).isNotNull();
        jarInputDir.close();
    }

    @Test
    public void testWithJarNull(@SuppressWarnings("unused") @Mocked final WsInputFile wsInputFile) throws Exception {
        final String warfile = "C/test.war";
        final String classesPath = "WEB-INF/classes";

        URLStreamHandler goodStubUrlHandler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return null;
            }
        };

        final URL goodURL = new URL("wsjar", "", -1, "file:" + warfile + "!/" + classesPath, goodStubUrlHandler);
        final URL warURL = new URL("wsjar", "", -1, "file:" + warfile, goodStubUrlHandler);

        new Expectations(URL.class) {
            {
                new URL("wsjar:file:" + warfile);
                result = warURL;

                Deencapsulation.invoke(warURL, "openStream");
                result = new IOException("dummy exception");
            }
        };

        WsInputDir jarInputDir = new WsInputDir(goodURL);
        jarInputDir.getFiles();

        for (@SuppressWarnings("unused")
                Vfs.File file : jarInputDir.getFiles()) {
            // nothing to check
        }

        new Verifications() {
            {
                new WsInputFile(classesPath, (ZipEntry) any, (JarInputStream) any);
                times = 0;
            }
        };
    }

    @Test(expected = SeedException.class)
    public void testPbOnJarToRead(@SuppressWarnings("unused") @Mocked final WsInputFile wsInputFile) throws Exception {
        final String warfile = "C/test.war";
        final String classesPath = "WEB-INF/classes";

        new MockUp<JarInputStream>() {
            @Mock
            public ZipEntry getNextEntry() throws IOException {
                throw new IOException("dummy exception");
            }
        };

        final MockUp<InputStream> inputStream = new MockUp<InputStream>() {
        };

        URLStreamHandler goodStubUrlHandler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return null;
            }
        };

        final URL goodURL = new URL("wsjar", "", -1, "file:" + warfile + "!/" + classesPath, goodStubUrlHandler);
        final URL warURL = new URL("wsjar", "", -1, "file:" + warfile, goodStubUrlHandler);

        new Expectations(URL.class) {
            {
                new URL("wsjar:file:" + warfile);
                result = warURL;

                Deencapsulation.invoke(warURL, "openStream");
                returns(inputStream.getMockInstance());
            }
        };

        WsInputDir jarInputDir = new WsInputDir(goodURL);
        jarInputDir.getFiles();

        for (@SuppressWarnings("unused")
                Vfs.File file : jarInputDir.getFiles()) {
            // nothing to check
        }
    }

    @Test
    public void testCloseJar() throws Exception {
        final String warfile = "C/test.war";
        final String classesPath = "WEB-INF/classes";

        final MockUp<JarInputStream> jarInputStream = new MockUp<JarInputStream>() {
            @Mock
            public void close() throws IOException {
                throw new IOException("dummy exception");
            }
        };

        final MockUp<InputStream> inputStream = new MockUp<InputStream>() {
        };

        final URLConnection goodUrlConnection = new MockUp<URLConnection>() {
            @Mock
            public InputStream getInputStream() {
                return inputStream.getMockInstance();
            }
        }.getMockInstance();

        URLStreamHandler goodStubUrlHandler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return goodUrlConnection;
            }
        };

        final URL goodURL = new URL("wsjar", "", -1, "file:" + warfile + "!/" + classesPath, goodStubUrlHandler);
        WsInputDir jarInputDir = new WsInputDir(goodURL);

        Deencapsulation.setField(jarInputDir, jarInputStream.getMockInstance());
        jarInputDir.close();

    }

}
