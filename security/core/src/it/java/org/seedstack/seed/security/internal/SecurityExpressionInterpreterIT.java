/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal;

import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.seed.security.WithUser;
import org.seedstack.seed.security.internal.securityexpr.SecurityExpressionInterpreter;

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
        Assertions.assertThat(
                interpreter.interpret("${hasPermission('lightSaber:*') && hasPermission('academy:*')}")).isTrue();
        Assertions.assertThat(
                interpreter.interpret("${hasPermission('lightSaber:*') || hasPermission('site:haunt')}")).isTrue();
        Assertions.assertThat(interpreter.interpret("${hasPermission('site:haunt')}")).isFalse();
        Assertions.assertThat(interpreter.interpret("${hasPermissionOn('lightSaber:*', 'MU')}")).isFalse();
    }

    @Test
    @WithUser(id = "ThePoltergeist", password = "bouh")
    public void check_security_method_with_scope_interpreted() {
        Assertions.assertThat(interpreter.interpret("${hasRole('ghost')}")).isTrue();
        Assertions.assertThat(interpreter.interpret("${hasRoleOn('ghost', 'MU')}")).isTrue();
        Assertions.assertThat(interpreter.interpret("${hasRoleOn('king', 'MU')}")).isFalse();
        Assertions.assertThat(interpreter.interpret("${hasRole('ghost') && hasRole('king')}")).isFalse();
        Assertions.assertThat(interpreter.interpret("${hasRole('ghost') || hasRole('king')}")).isTrue();
    }
}
