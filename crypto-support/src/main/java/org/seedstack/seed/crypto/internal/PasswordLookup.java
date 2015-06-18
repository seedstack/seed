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

import java.security.InvalidKeyException;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.text.StrLookup;
import org.seedstack.seed.crypto.api.EncryptionService;

/**
 * {@link Configuration} lookup for a parameter like ${password:xxxx} where xxxx is an encrypted password.
 * 
 * @author thierry.bouvet@mpsa.com
 */
public class PasswordLookup extends StrLookup {

    private EncryptionService service;

    public PasswordLookup(EncryptionService service) {
        this.service = service;
    }

    @Override
    public String lookup(String key) {
        try {
            return new String(service.decrypt(DatatypeConverter.parseHexBinary(key)));
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Can not decrypt passwords !", e);
        }
    }

}
