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
import org.seedstack.seed.core.spi.configuration.ConfigurationLookup;
import org.seedstack.seed.crypto.api.EncryptionService;

import javax.xml.bind.DatatypeConverter;
import java.security.InvalidKeyException;

/**
 * {@link Configuration} lookup for a parameter like ${password:xxxx} where xxxx is an encrypted password.
 *
 * @author thierry.bouvet@mpsa.com
 */
@ConfigurationLookup("password")
public class PasswordLookup extends StrLookup {
    private final EncryptionService encryptionService;

    public PasswordLookup() {
        EncryptionServiceFactory serviceFactory = new EncryptionServiceFactory();
        encryptionService = serviceFactory.createEncryptionService();
    }

    @Override
    public String lookup(String key) {
        try {
            return new String(encryptionService.decrypt(DatatypeConverter.parseHexBinary(key)));
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Can not decrypt passwords !", e);
        }
    }

}
