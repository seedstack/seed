/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.seedstack.seed.security.AuthenticationToken;

public class SeedUsernamePasswordToken extends UsernamePasswordToken implements AuthenticationToken {
    public SeedUsernamePasswordToken(String username, char[] password, boolean rememberMe, String host) {
        super(username, password, rememberMe, host);
    }
}
