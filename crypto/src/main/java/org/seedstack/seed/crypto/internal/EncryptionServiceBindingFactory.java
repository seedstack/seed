/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import com.google.inject.Key;
import com.google.inject.name.Names;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.spi.dependency.Maybe;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.crypto.api.EncryptionService;

import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import static org.seedstack.seed.core.utils.ConfigurationUtils.buildKey;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
class EncryptionServiceBindingFactory {

    Map<Key<EncryptionService>, EncryptionService> createBindings(Configuration configuration, Map<String, KeyStoreConfig> keyStoreConfigurations, Map<String, KeyStore> keyStores) {
        Map<Key<EncryptionService>, EncryptionService> encryptionServices = new HashMap<Key<EncryptionService>, EncryptionService>();

        if (keyStoreConfigurations != null) {
            for (Map.Entry<String, KeyStoreConfig> entry : keyStoreConfigurations.entrySet()) {
                String keyStoreName = entry.getKey();

                if (keyStores != null) {
                    EncryptionServiceFactory serviceFactory = new EncryptionServiceFactory(configuration, keyStores.get(keyStoreName));

                    for (Map.Entry<String, String> aliasPasswordEntry : entry.getValue().getAliasPasswords().entrySet()) {
                        String alias = aliasPasswordEntry.getKey();

                        EncryptionService encryptionService = serviceFactory.create(alias, aliasPasswordEntry.getValue().toCharArray());

                        String qualifier = getAliasQualifier(configuration, keyStoreName, alias);
                        if (qualifier != null) {
                            encryptionServices.put(createKeyFromQualifier(qualifier), encryptionService);
                        } else {
                            encryptionServices.put(Key.get(EncryptionService.class, Names.named(alias)), encryptionService);
                        }
                    }
                }
            }
        }
        return encryptionServices;
    }

    private String getAliasQualifier(Configuration configuration, String keyStoreName, String alias) {
        String configurationKey = buildKey(CryptoPlugin.KEYSTORE, keyStoreName, "alias", alias, "qualifier");
        return configuration.getString(configurationKey);
    }

    private Key<EncryptionService> createKeyFromQualifier(String qualifier) {
        Key<EncryptionService> key;
        Maybe<Class<?>> classMaybe = SeedReflectionUtils.forName(qualifier);
        if (classMaybe.isPresent()) {
            Class<?> qualifierClass = classMaybe.get();
            if (!Annotation.class.isAssignableFrom(qualifierClass) || !qualifierClass.isAnnotationPresent(Qualifier.class)) {
                throw SeedException.createNew(CryptoErrorCodes.INVALID_QUALIFIER_ANNOTATION).put("qualifier", qualifier);
            }
            //noinspection unchecked
            key = Key.get(EncryptionService.class, (Class<? extends Annotation>) qualifierClass);
        } else {
            key = Key.get(EncryptionService.class, Names.named(qualifier));
        }
        return key;
    }
}
