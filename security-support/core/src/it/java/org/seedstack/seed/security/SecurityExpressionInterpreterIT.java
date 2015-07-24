/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.seed.security.api.WithUser;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.security.internal.securityexpr.SecurityExpressionInterpreter;

import javax.inject.Inject;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 29/07/2014
 */
@RunWith(SeedITRunner.class)
public class SecurityExpressionInterpreterIT {

    @Inject
    private SecurityExpressionInterpreter interpreter;

    @Test
    public void test_interpreter_with_boolean() {
        Assertions.assertThat(interpreter.interpret(Boolean.FALSE)).isFalse();
        Assertions.assertThat(interpreter.interpret(false)).isFalse();
        Assertions.assertThat(interpreter.interpret(Boolean.TRUE)).isTrue();
        Assertions.assertThat(interpreter.interpret(true)).isTrue();
    }

    @Test
    @WithUser(id = "Obiwan", password = "yodarulez")
    public void check_security_permission_methods_interpreted() {
        Assertions.assertThat(interpreter.interpret("${hasAllPermissions('lightSaber:*', 'academy:*')}")).isTrue();
        Assertions.assertThat(interpreter.interpret("${hasOnePermission('lightSaber:*', 'site:haunt')}")).isTrue();
        Assertions.assertThat(interpreter.interpret("${hasOnePermission('site:haunt')}")).isFalse();
        Assertions.assertThat(interpreter.interpret("${hasPermission('site:haunt')}")).isFalse();
        Assertions.assertThat(interpreter.interpret("${hasPermission('lightSaber:*', 'MU')}")).isFalse();
    }

    @Test
    @WithUser(id = "ThePoltergeist", password = "bouh")
    public void check_security_method_with_scope_interpreted() {
        Assertions.assertThat(interpreter.interpret("${hasRole('ghost')}")).isTrue();
        Assertions.assertThat(interpreter.interpret("${hasRole('ghost', 'MU')}")).isTrue();
        Assertions.assertThat(interpreter.interpret("${hasRole('king', 'MU')}")).isFalse();
        Assertions.assertThat(interpreter.interpret("${hasAllRoles('ghost')}")).isTrue();
        Assertions.assertThat(interpreter.interpret("${hasAllRoles('ghost', 'king')}")).isFalse();
        Assertions.assertThat(interpreter.interpret("${hasOneRole('ghost', 'king')}")).isTrue();
    }
}
