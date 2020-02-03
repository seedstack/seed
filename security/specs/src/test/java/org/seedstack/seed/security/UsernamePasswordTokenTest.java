/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class UsernamePasswordTokenTest {

    @Test
    public void testToken() {
        String user = "user";
        String password = "psw";
        UsernamePasswordToken token = new UsernamePasswordToken(user, password);
        assertThat(token.getUsername()).isEqualTo(user);
        assertThat(new String(token.getPassword())).isEqualTo(password);
    }
}
