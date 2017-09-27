/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal.scan.websphere;

import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import mockit.Mock;
import mockit.MockUp;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class WsInputFileTest {

    @Test
    public void testGetName() {
        final String classesPath = "WEB-INF/classes/";
        final String filename = "test.props";
        final String entryName = "META-INF/configuration/" + filename;
        final String pathName = classesPath + entryName;
        MockUp<ZipEntry> entry = new MockUp<ZipEntry>() {
            @Mock
            public String getName() {
                return pathName;
            }
        };
        MockUp<JarInputStream> jarInputStream = new MockUp<JarInputStream>() {
        };
        WsInputFile jarInputFile = new WsInputFile(classesPath, entry.getMockInstance(),
                jarInputStream.getMockInstance());

        Assertions.assertThat(jarInputFile.getName()).isEqualTo(filename);
        Assertions.assertThat(jarInputFile.getRelativePath()).isEqualTo(entryName);
    }
}
