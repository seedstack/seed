/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.crypto;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.Collection;
import javax.xml.bind.DatatypeConverter;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.cli.CliArgs;
import org.seedstack.seed.cli.CliOption;
import org.seedstack.seed.core.internal.AbstractSeedTool;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.seed.crypto.EncryptionService;

public class CryptTool extends AbstractSeedTool {
    private EncryptionServiceFactory encryptionServiceFactory;
    private CryptoConfig.KeyStoreConfig masterKeyStoreConfig;
    @CliOption(name = "a", longName = "alias", valueCount = 1, mandatory = true)
    private String alias;
    @CliOption(name = "e", longName = "encoding", valueCount = 1, defaultValues = "utf-8")
    private String encoding;
    @CliArgs(mandatoryCount = 1)
    private String[] args;

    @Override
    public String toolName() {
        return "crypt";
    }

    @Override
    protected Collection<Class<?>> toolPlugins() {
        return Lists.newArrayList(CryptoPlugin.class);
    }

    @Override
    protected InitState initialize(InitContext initContext) {
        CryptoConfig cryptoConfig = getConfiguration(CryptoConfig.class);
        masterKeyStoreConfig = cryptoConfig.masterKeyStore();
        if (masterKeyStoreConfig != null) {
            KeyStore keyStore = new KeyStoreLoader().load(CryptoConfig.MASTER_KEY_STORE_NAME, masterKeyStoreConfig);
            encryptionServiceFactory = new EncryptionServiceFactory(cryptoConfig, keyStore);
        } else {
            encryptionServiceFactory = null;
        }
        return InitState.INITIALIZED;
    }

    @Override
    public Integer call() throws Exception {
        if (encryptionServiceFactory == null) {
            throw SeedException.createNew(CryptoErrorCode.MISSING_MASTER_KEYSTORE);
        }
        CryptoConfig.KeyStoreConfig.AliasConfig aliasConfig = masterKeyStoreConfig.getAliases().get(alias);
        if (aliasConfig == null || Strings.isNullOrEmpty(aliasConfig.getPassword())) {
            throw SeedException.createNew(CryptoErrorCode.MISSING_MASTER_KEY_PASSWORD);
        }
        EncryptionService encryptionService = encryptionServiceFactory.create(alias,
                aliasConfig.getPassword().toCharArray());
        System.out.println(
                DatatypeConverter.printHexBinary(
                        encryptionService.encrypt(
                                args[0].getBytes(Charset.forName(encoding))
                        )
                )
        );
        return 0;
    }
}
