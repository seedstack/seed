/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
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
import org.seedstack.seed.Application;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.spi.configuration.ConfigurationLookup;
import org.seedstack.seed.crypto.EncryptionService;

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
// Should stay public to be scanned by the service loader
@ConfigurationLookup("password")
public class PasswordLookup extends StrLookup {

    private final EncryptionService encryptionService;

    public PasswordLookup(Application application) {
        Configuration cryptoConfig = application.getConfiguration().subset(CryptoPlugin.CONFIG_PREFIX);
        if (cryptoConfig.containsKey(CryptoPlugin.MASTER_KEYSTORE_PATH)) {
            KeyStoreConfig ksConfig = new KeyStoreConfigFactory(cryptoConfig).create(MASTER_KEYSTORE_NAME);
            KeyStore keyStore = new KeyStoreLoader().load(ksConfig);
            String pass = ksConfig.getAliasPasswords().get(MASTER_KEY_NAME);
            if (pass == null || pass.equals("")) {
                throw SeedException.createNew(CryptoErrorCodes.MISSING_MASTER_KEY_PASSWORD);
            }
            char[] password = pass.toCharArray();
            encryptionService = new EncryptionServiceFactory(application.getConfiguration(), keyStore).create(MASTER_KEY_NAME, password);
        } else {
            encryptionService = null;
        }
    }

    @Override
    public String lookup(String key) {
        if (encryptionService == null) {
            throw SeedException.createNew(CryptoErrorCodes.MISSING_MASTER_KEYSTORE)
                    .put("keyPassword", "${env:KEY_PASSWD}").put("password", "${env:KS_PASSWD}");
        }
        try {
            return new String(encryptionService.decrypt(DatatypeConverter.parseHexBinary(key)));
        } catch (InvalidKeyException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.UNEXPECTED_EXCEPTION);
        }
    }

}
