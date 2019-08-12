/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.crypto;

import static org.seedstack.seed.core.internal.crypto.CryptoPlugin.getMasterEncryptionService;

import com.google.common.io.BaseEncoding;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import java.nio.charset.Charset;
import java.security.KeyStore;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.cli.CliArgs;
import org.seedstack.seed.cli.CliOption;
import org.seedstack.seed.core.internal.AbstractSeedTool;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.seed.crypto.EncryptionService;

public class CryptTool extends AbstractSeedTool {
    private EncryptionServiceFactory encryptionServiceFactory;
    private CryptoConfig.KeyStoreConfig masterKeyStoreConfig;
    @CliOption(name = "a", longName = "alias", valueCount = 1, defaultValues = "master")
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
    protected InitState initialize(InitContext initContext) {
        getConfiguration().getOptional(CryptoConfig.KeyStoreConfig.class, "crypto.keystores.master").ifPresent(cfg -> {
            KeyStore keyStore = new KeyStoreLoader().load(CryptoConfig.MASTER_KEY_STORE_NAME, cfg);
            encryptionServiceFactory = new EncryptionServiceFactory(keyStore);
            masterKeyStoreConfig = cfg;
        });
        return InitState.INITIALIZED;
    }

    @Override
    public StartMode startMode() {
        return StartMode.MINIMAL;
    }

    @Override
    public Integer call() {
        if (encryptionServiceFactory == null) {
            throw SeedException.createNew(CryptoErrorCode.MISSING_MASTER_KEYSTORE);
        }
        EncryptionService encryptionService = getMasterEncryptionService(encryptionServiceFactory,
                masterKeyStoreConfig,
                alias);
        System.out.println(
                BaseEncoding.base16().encode(
                        encryptionService.encrypt(
                                args[0].getBytes(Charset.forName(encoding))
                        )
                )
        );
        return 0;
    }
}
