/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.core.internal.KernelCoreFactory;
import io.nuun.kernel.core.internal.ModuleEmbedded;
import org.assertj.core.api.Assertions;
import org.junit.*;

import static io.nuun.kernel.core.NuunCore.createKernel;
import static io.nuun.kernel.core.NuunCore.newKernelConfiguration;

/**
 * @author Pierre THIROUIN (pierre.thirouin@ext.inetpsa.com)
 */
public class IgnoreIT {

    private static Kernel kernel;
    private static Injector injector;

    @org.seedstack.seed.Ignore
    @Scan
    static class IgnoredClass {
    }

    @Scan
    static class ScannedClass {
    }

    @interface Scan {}

    @BeforeClass
    public static void setUp() throws Exception {
        kernel = createKernel(newKernelConfiguration().withoutSpiPluginsLoader().addPlugin(IgnorePlugin.class));
        kernel.init();
        kernel.start();
        injector = kernel.objectGraph().as(Injector.class);
    }

    @Test
    public void testScanWorks() throws Exception {
        ScannedClass instance = injector.getInstance(ScannedClass.class);
        Assertions.assertThat(instance).isNotNull();
    }

    @Test(expected = ConfigurationException.class)
    public void testIgnoreFeature() throws Exception {
        injector.getInstance(IgnoredClass.class);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        kernel.stop();
    }
}
