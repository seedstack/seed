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
 * Creation : 8 juin 2015
 */
package org.seedstack.seed.crypto.internal;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.text.StrLookup;
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.spi.configuration.ConfigurationLookup;
import org.seedstack.seed.crypto.api.EncryptionService;

import javax.xml.bind.DatatypeConverter;
import java.security.InvalidKeyException;
import java.security.KeyStore;

import static org.seedstack.seed.crypto.internal.CryptoPlugin.MASTER_KEYSTORE_NAME;
import static org.seedstack.seed.crypto.internal.CryptoPlugin.MASTER_KEY_NAME;

/**
 * {@link Configuration} lookup for a parameter like ${password:xxxx} where xxxx is an encrypted password.
 *
 * @author thierry.bouvet@mpsa.com
 */
@ConfigurationLookup("password")
public class PasswordLookup extends StrLookup {

    private final EncryptionService encryptionService;

    public PasswordLookup(Application application) {
        Configuration cryptoConfig = application.getConfiguration().subset(CryptoPlugin.CRYPTO_PLUGIN_PREFIX);
        if (cryptoConfig.containsKey(CryptoPlugin.MASTER_KEYSTORE)) {
            KeyStoreConfig ksConfig = new KeyStoreConfigFactory(cryptoConfig).create(MASTER_KEYSTORE_NAME);
            KeyStore keyStore = new KeyStoreLoader().load(ksConfig);
            String pass = ksConfig.getAliasPasswords().get(MASTER_KEY_NAME);
            if (pass == null || pass.equals("")) {
                throw new IllegalArgumentException("Missing master key password");
            }
            char[] password = pass.toCharArray();
            encryptionService = new EncryptionServiceFactory(application, keyStore).create(MASTER_KEY_NAME, password);
        } else {
            encryptionService = null;
        }
    }

    @Override
    public String lookup(String key) {
        if (encryptionService == null) {
            throw new IllegalStateException("The \"password\" lookup can not be used since no master KeyStore is configured.");
        }
        try {
            return new String(encryptionService.decrypt(DatatypeConverter.parseHexBinary(key)));
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Can not decrypt passwords !", e);
        }
    }

}
