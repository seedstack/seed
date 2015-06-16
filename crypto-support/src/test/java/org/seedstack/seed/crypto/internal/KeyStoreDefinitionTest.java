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
 * Creation : 10 juin 2015
 */
/**
 * 
 */
package org.seedstack.seed.crypto.internal;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Unit test for {@link KeyStoreDefinition}
 * 
 * @author thierry.bouvet@mpsa.com
 */
public class KeyStoreDefinitionTest {

    /**
     * Test method for {@link org.seedstack.seed.crypto.internal.KeyStoreDefinition#getPath()}.
     */
    @Test
    public void testGetPath() {
        KeyStoreDefinition definition = new KeyStoreDefinition();
        final String path = "path";
        definition.setPath(path);
        Assertions.assertThat(definition.getPath()).isEqualTo(path);
    }

    /**
     * Test method for {@link org.seedstack.seed.crypto.internal.KeyStoreDefinition#getPassword()}.
     */
    @Test
    public void testGetPassword() {
        KeyStoreDefinition definition = new KeyStoreDefinition();
        final String password = "password";
        definition.setPassword(password);
        Assertions.assertThat(definition.getPassword()).isEqualTo(password);
    }

}
