/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.crypto.Hash;

public class PBKDF2HashingSupportTest {

    private PBKDF2HashingService hashingSupport;

    @Before
    public void before() {
        hashingSupport = new PBKDF2HashingService();
    }

    @Test
    public void testSomeHashings() {
        String toHash = "p\r\nassw0Rd!";
        Hash hash1 = hashingSupport.createHash(toHash);
        Hash hash2 = hashingSupport.createHash(toHash);
        assertThat(hash1.getHash()).isNotEqualTo(hash2.getHash());
        assertThat(hash1.getSalt()).isNotEqualTo(hash2.getSalt());

        // Test password validation
        for (int i = 0; i < 10; i++) {
            String password = "" + i;
            Hash hash = hashingSupport.createHash(password);
            String wrongPassword = "" + (i + 1);
            assertThat(hashingSupport.validatePassword(wrongPassword, hash)).isFalse();

            assertThat(hashingSupport.validatePassword(password, hash)).isTrue();
        }
    }
}
