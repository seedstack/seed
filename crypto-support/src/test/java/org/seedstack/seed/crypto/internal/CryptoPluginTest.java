/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 4 juin 2015
 */
/**
 *
 */
package org.seedstack.seed.crypto.internal;

import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.apache.commons.configuration.Configuration;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.crypto.api.EncryptionService;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link CryptoPlugin}.
 *
 * @author thierry.bouvet@mpsa.com
 */
public class CryptoPluginTest {

    /**
     * Test method for {@link org.seedstack.seed.crypto.internal.CryptoPlugin#name()}.
     */
    @Test
    public void testName() {
        Assertions.assertThat(new CryptoPlugin().name()).isEqualTo("seed-crypto-plugin");
    }

    /**
     * Test method for {@link org.seedstack.seed.crypto.internal.CryptoPlugin#nativeUnitModule()}.
     */
    @Test
    public void testNativeUnitModule(@SuppressWarnings("unused") @Mocked final CryptoModule module) {

        final Map<String, EncryptionService> encryptionServices = new HashMap<String, EncryptionService>();

        final CryptoPlugin plugin = new CryptoPlugin();
        Deencapsulation.setField(plugin, "encryptionServices", encryptionServices);

        plugin.nativeUnitModule();
        new Verifications() {
            {
                new CryptoModule(encryptionServices);
                times = 1;
            }
        };

    }

    /**
     * Test method for {@link org.seedstack.seed.crypto.internal.CryptoPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}. Test creation of
     * {@link EncryptionService}.
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testInitInitContext(@Mocked final InitContext context, @Mocked final ApplicationPlugin applicationPlugin,
                                    @Mocked final Configuration configuration, @Mocked final Application application, @Mocked final EncryptionServiceFactory factory) throws Exception {

        new Expectations() {
            @Mocked
            KeyDefinition keyDefinition;

            {
                context.pluginsRequired();
                result = Collections.singleton(applicationPlugin);

                applicationPlugin.getApplication();
                result = application;

                application.getConfiguration();
                result = configuration;

                configuration.subset("org.seedstack.seed.crypto");
                result = configuration;

                configuration.getStringArray("keys");
                result = new String[] { "key1" };

            }
        };
        final CryptoPlugin plugin = new CryptoPlugin();
        plugin.init(context);

        new Verifications() {
            Map<String, EncryptionService> encryptionServices = Deencapsulation.getField(plugin, "encryptionServices");

            {
                factory.createEncryptionService(); //master
                factory.createEncryptionService((KeyDefinition) any); //key1

                Assertions.assertThat(encryptionServices.size()).isEqualTo(2);
            }
        };

    }

    /**
     * Test method for {@link org.seedstack.seed.crypto.internal.CryptoPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}. Test creation of
     * {@link EncryptionService} with a custom keystore for one key.
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testInitInitContextWithCustomKeystoreForKey(@Mocked final InitContext context, @Mocked final ApplicationPlugin applicationPlugin,
                                                            @Mocked final Configuration configuration, @Mocked final Configuration subsetConfiguration, @Mocked final Application application, @Mocked final EncryptionServiceFactory factory) throws Exception {

        new Expectations() {
            @Mocked
            KeyDefinition keyDefinition;

            {
                context.pluginsRequired();
                result = Collections.singleton(applicationPlugin);

                applicationPlugin.getApplication();
                result = application;

                application.getConfiguration();
                result = configuration;

                configuration.subset("org.seedstack.seed.crypto");
                result = configuration;

                configuration.getStringArray("keys");
                result = new String[] { "key1" };
            }
        };
        final CryptoPlugin plugin = new CryptoPlugin();
        plugin.init(context);

        new Verifications() {
            Map<String, EncryptionService> encryptionServices = Deencapsulation.getField(plugin, "encryptionServices");
            final int configurations = 2; // master + key1

            {
                factory.createEncryptionService();
                factory.createEncryptionService((KeyDefinition) any);

                Assertions.assertThat(encryptionServices.size()).isEqualTo(configurations);
            }
        };

    }

    /**
     * Test method for {@link org.seedstack.seed.crypto.internal.CryptoPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}. Test an error in
     * the configuration file.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = RuntimeException.class)
    public void testInitInitContextWithProblemKeyDefinition(@Mocked final InitContext context, @Mocked final ApplicationPlugin applicationPlugin,
                                                            @Mocked final Configuration configuration, @Mocked final Application application) throws Exception {

        new Expectations() {
            @Mocked
            KeyDefinition keyDefinition;

            {
                context.pluginsRequired();
                result = Collections.singleton(applicationPlugin);

                applicationPlugin.getApplication();
                result = application;

                application.getConfiguration();
                result = configuration;

                configuration.subset("org.seedstack.seed.crypto");
                result = configuration;

                configuration.getStringArray("keys");
                result = new String[] { "key1" };

                configuration.isEmpty();
                result = true;
            }
        };
        final CryptoPlugin plugin = new CryptoPlugin();
        plugin.init(context);

    }

    /**
     * Test method for {@link org.seedstack.seed.crypto.internal.CryptoPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}. Test the plugin
     * without {@link ApplicationPlugin}. So nothing is done.
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testInitInitContextWithoutApplicationContext(@Mocked final InitContext context, @Mocked final AbstractPlugin applicationPlugin)
            throws Exception {

        new Expectations() {
            @Mocked
            KeyDefinition keyDefinition;

            {
                context.pluginsRequired();
                result = Collections.singleton(applicationPlugin);

            }
        };
        final CryptoPlugin plugin = new CryptoPlugin();
        plugin.init(context);

        new Verifications() {
            Map<String, EncryptionService> encryptionServices = Deencapsulation.getField(plugin, "encryptionServices");

            {
                Assertions.assertThat(encryptionServices).isEmpty();
            }
        };

    }

    /**
     * Test method for {@link org.seedstack.seed.crypto.internal.CryptoPlugin#requiredPlugins()}.
     */
    @Test
    public void testRequiredPlugins() {
        CryptoPlugin plugin = new CryptoPlugin();
        Collection<Class<? extends Plugin>> list = plugin.requiredPlugins();
        Assertions.assertThat(list.contains(ApplicationPlugin.class)).isTrue();
    }
}
