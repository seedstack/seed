/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
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

import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.Verifications;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.crypto.api.EncryptionService;

import java.security.KeyStore;
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
        final Map<String, KeyStore> keyStores = new HashMap<String, KeyStore>();

        final CryptoPlugin plugin = new CryptoPlugin();
        Deencapsulation.setField(plugin, "encryptionServices", encryptionServices);
        Deencapsulation.setField(plugin, "keyStores", keyStores);

        plugin.nativeUnitModule();
        new Verifications() {
            {
                new CryptoModule(encryptionServices, keyStores);
                times = 1;
            }
        };

    }
//
//    /**
//     * Test method for {@link org.seedstack.seed.crypto.internal.CryptoPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}. Test creation of
//     * {@link EncryptionService}.
//     *
//     * @throws Exception if an error occurred
//     */
//    @Test
//    public void testInitInitContext(@Mocked final InitContext context, @Mocked final ApplicationPlugin applicationPlugin,
//                                    @Mocked final Configuration configuration, @Mocked final EncryptionServiceFactory factory,
//                                    @Mocked final CryptoDefinitionFactory cryptoDefinitionFactory, @Mocked final KeyStore keystore) throws Exception {
//        new Expectations() {
//
//            {
//                context.pluginsRequired();
//                result = Collections.singleton(applicationPlugin);
//
//                configuration.containsKey(CryptoPlugin.MASTER_KEYSTORE);
//                result = false;
//
//                cryptoDefinitionFactory.loadDefaultKeyStore();
//                result = keystore;
//
//                cryptoDefinitionFactory.loadKeyDefinitions(keystore);
//                KeyDefinition keyDefinition1 = new KeyDefinition();
//                keyDefinition1.setKeyName("key1");
//                KeyDefinition keyDefinition2 = new KeyDefinition();
//                keyDefinition2.setKeyName("key2");
//                result = Lists.newArrayList(keyDefinition1, keyDefinition2);
//            }
//        };
//        final CryptoPlugin plugin = new CryptoPlugin();
//        plugin.init(context);
//
//        new Verifications() {
//            Map<String, EncryptionService> encryptionServices = Deencapsulation.getField(plugin, "encryptionServices");
//
//            {
//                factory.create((KeyDefinition) any);
//                times = 2;
//
//                Assertions.assertThat(encryptionServices.size()).isEqualTo(2);
//            }
//        };
//    }
//
//    @Test
//    public void testInitWithoutMasterKey(@Mocked final InitContext context, @Mocked final ApplicationPlugin applicationPlugin,
//                                         @Mocked final Configuration configuration, @Mocked final Application application,
//                                         @Mocked final EncryptionServiceFactory factory, @Mocked final CryptoDefinitionFactory cryptoDefinitionFactory) throws Exception {
//        new Expectations() {
//            @Mocked
//            KeyDefinition keyDefinition;
//
//            {
//                context.pluginsRequired();
//                result = Collections.singleton(applicationPlugin);
//
//                applicationPlugin.getApplication();
//                result = application;
//
//                application.getConfiguration();
//                result = configuration;
//
//                configuration.subset(CryptoPlugin.CRYPTO_PLUGIN_PREFIX);
//                result = configuration;
//
//                configuration.containsKey(CryptoPlugin.MASTER_KEYSTORE);
//                result = false;
//            }
//        };
//        final CryptoPlugin plugin = new CryptoPlugin();
//        plugin.init(context);
//
//        new Verifications() {
//            Map<String, EncryptionService> encryptionServices = Deencapsulation.getField(plugin, "encryptionServices");
//
//            {
//                factory.create((KeyDefinition) any);
//                times = 0;
//
//                Assertions.assertThat(encryptionServices.size()).isEqualTo(0);
//            }
//        };
//    }
//
//    @Test
//    public void testInitWithMasterKey(@Mocked final InitContext context, @Mocked final ApplicationPlugin applicationPlugin,
//                                      @Mocked final Configuration configuration, @Mocked final Application application,
//                                      @Mocked final CryptoDefinitionFactory cryptoDefinitionFactory) throws Exception {
//        new Expectations() {
//            @Mocked
//            KeyDefinition keyDefinition;
//
//            {
//                context.pluginsRequired();
//                result = Collections.singleton(applicationPlugin);
//
//                applicationPlugin.getApplication();
//                result = application;
//
//                application.getConfiguration();
//                result = configuration;
//
//                configuration.subset(CryptoPlugin.CRYPTO_PLUGIN_PREFIX);
//                result = configuration;
//
//                configuration.containsKey(CryptoPlugin.MASTER_KEYSTORE);
//                result = true;
//            }
//        };
//        final CryptoPlugin plugin = new CryptoPlugin();
//        plugin.init(context);
//
//        new Verifications() {
//            {
//                cryptoDefinitionFactory.getMasterKeyDefinition();
//                times = 1;
//            }
//        };
//    }
//
//    /**
//     * The SslDefinition and the SslContext should be initialized by the automatic generation.
//     */
//    @Test
//    public void testInitWithSSLAutoMode(@Mocked final InitContext context, @Mocked final ApplicationPlugin applicationPlugin,
//                                        @Mocked final Configuration cryptoConfig, @Mocked final SsLLoader ssLLoader) throws Exception {
//        new Expectations() {
//            {
//                context.pluginsRequired();
//                result = Collections.singleton(applicationPlugin);
//
//                cryptoConfig.containsKey(CryptoPlugin.MASTER_KEYSTORE);
//                result = false;
//                cryptoConfig.containsKey("ssl.keystore.path");
//                result = false;
//                cryptoConfig.getBoolean("ssl.auto", false);
//                result = true;
//                new SsLLoader(cryptoConfig);
//                result = ssLLoader;
//            }
//        };
//
//        final CryptoPlugin plugin = new CryptoPlugin();
//        plugin.init(context);
//
//        new Verifications() {
//            {
//                ssLLoader.createSslDefinition(true);
//                times = 1;
//            }
//        };
//    }
//
//    /**
//     * SSL should be enable as the ssl keystore path is specified.
//     * But throw an exception as the keystore can't be loaded.
//     */
//    @Test(expected = SeedException.class)
//    public void testInitWithSSL(@Mocked final InitContext context,
//                                @Mocked final ApplicationPlugin applicationPlugin,
//                                @Mocked final Configuration configuration,
//                                @Mocked final Application application) throws Exception {
//        new Expectations() {
//
//            {
//                context.pluginsRequired();
//                result = Collections.singleton(applicationPlugin);
//
//                applicationPlugin.getApplication();
//                result = application;
//
//                application.getConfiguration();
//                result = configuration;
//
//                configuration.subset(CryptoPlugin.CRYPTO_PLUGIN_PREFIX);
//                result = configuration;
//
//                configuration.containsKey("ssl.keystore.path");
//                result = true;
//            }
//        };
//        new CryptoPlugin().init(context);
//    }
//
//    /**
//     * Test method for {@link org.seedstack.seed.crypto.internal.CryptoPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}. Test an error in
//     * the configuration file.
//     *
//     * @throws Exception if an error occurred
//     */
//    @Test(expected = RuntimeException.class)
//    public void testInitInitContextWithProblemKeyDefinition(@Mocked final InitContext context, @Mocked final ApplicationPlugin applicationPlugin,
//                                                            @Mocked final Configuration configuration, @Mocked final Application application) throws Exception {
//
//        new Expectations() {
//            @Mocked
//            KeyDefinition keyDefinition;
//
//            {
//                context.pluginsRequired();
//                result = Collections.singleton(applicationPlugin);
//
//                applicationPlugin.getApplication();
//                result = application;
//
//                application.getConfiguration();
//                result = configuration;
//
//                configuration.subset(CryptoPlugin.CRYPTO_PLUGIN_PREFIX);
//                result = configuration;
//
//                configuration.getStringArray("keys");
//                result = new String[]{"key1"};
//            }
//        };
//        final CryptoPlugin plugin = new CryptoPlugin();
//        plugin.init(context);
//
//    }
//
//    /**
//     * Test method for {@link org.seedstack.seed.crypto.internal.CryptoPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}. Test the plugin
//     * without {@link ApplicationPlugin}. So nothing is done.
//     *
//     * @throws Exception if an error occurred
//     */
//    @Test(expected = PluginException.class)
//    public void testInitInitContextWithoutApplicationContext(@Mocked final InitContext context, @Mocked final AbstractPlugin applicationPlugin)
//            throws Exception {
//
//        new Expectations() {
//            @Mocked
//            KeyDefinition keyDefinition;
//
//            {
//                context.pluginsRequired();
//                result = Collections.singleton(applicationPlugin);
//
//            }
//        };
//        final CryptoPlugin plugin = new CryptoPlugin();
//        plugin.init(context);
//    }
//
//    /**
//     * Test method for {@link org.seedstack.seed.crypto.internal.CryptoPlugin#requiredPlugins()}.
//     */
//    @Test
//    public void testRequiredPlugins() {
//        CryptoPlugin plugin = new CryptoPlugin();
//        Collection<Class<? extends Plugin>> list = plugin.requiredPlugins();
//        Assertions.assertThat(list.contains(ApplicationPlugin.class)).isTrue();
//    }
}
