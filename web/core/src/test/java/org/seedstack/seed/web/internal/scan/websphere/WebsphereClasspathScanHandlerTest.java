/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal.scan.websphere;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.List;
import mockit.Mocked;
import mockit.Verifications;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.reflections.vfs.Vfs;

public class WebsphereClasspathScanHandlerTest {

    @Test
    public void testUrlTypes(@SuppressWarnings("unused") @Mocked final WsInputDir wsInputDir) throws Exception {
        WebsphereClasspathScanHandler handler = new WebsphereClasspathScanHandler();
        List<Vfs.UrlType> list = handler.urlTypes();
        Assertions.assertThat(list.size()).isEqualTo(1);
        Vfs.UrlType urlType = list.get(0);

        URLStreamHandler goodStubUrlHandler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return null;
            }
        };

        final URL warURL = new URL("wsjar", "", -1, "file:C/test.war", goodStubUrlHandler);
        final URL warJarURL = new URL("wsjar", "", -1, "file:C/test.war!test.jar", goodStubUrlHandler);
        final URL badURL = new URL("jar", "", -1, "file:C/test.war");
        Assertions.assertThat(urlType.matches(warURL)).isTrue();
        Assertions.assertThat(urlType.matches(badURL)).isFalse();
        Assertions.assertThat(urlType.matches(warJarURL)).isFalse();

        urlType.createDir(warURL);
        new Verifications() {
            {
                new WsInputDir(warURL);
                times = 1;
            }
        };
    }

}
