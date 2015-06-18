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
 * Creation : 12 mars 2015
 */
package org.seedstack.seed.crypto.internal;

import java.util.Map;
import java.util.Map.Entry;

import org.seedstack.seed.crypto.api.EncryptionService;
import org.seedstack.seed.crypto.api.HashingService;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;

class CryptoModule extends AbstractModule {

    private Map<String, EncryptionService> rsaServices;

    public CryptoModule(Map<String, EncryptionService> rsaServices) {
        this.rsaServices = rsaServices;
    }

    @Override
    protected void configure() {
        for (Entry<String, EncryptionService> definition : this.rsaServices.entrySet()) {
            bind(Key.get(EncryptionService.class, Names.named(definition.getKey()))).toInstance(definition.getValue());
        }
        bind(HashingService.class).to(PBKDF2HashingService.class);
    }

}
