/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import javax.el.ELContext;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.el.ELContextBuilder;
import org.seedstack.seed.el.ELService;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.seedstack.seed.security.internal.securityexpr.SecurityExpressionUtils;

@RunWith(SeedITRunner.class)
public class SecurityExpressionUtilsIT {
    @Inject
    private ELService elService;
    @Inject
    private ELContextBuilder elContextBuilder;

    @Test
    @WithUser(id = "Obiwan", password = "yodarulez")
    public void simpleElSecurityIntegrationCheck() throws SecurityException, NoSuchMethodException {
        assertThat(expression("${  hasRole('jedi') }")).isTrue();
    }

    @Test
    @WithUser(id = "Anakin", password = "imsodark")
    public void composedElSecurityIntegrationCheck() throws SecurityException, NoSuchMethodException {
        assertThat(expression("${ ! hasRole('jedi') && hasPermission('academy:learn')  }")).isTrue();
    }

    private Boolean expression(String expression) throws SecurityException, NoSuchMethodException {
        Method m = SecurityExpressionUtils.class.getDeclaredMethod("hasRole", String.class);
        Method p = SecurityExpressionUtils.class.getDeclaredMethod("hasPermission", String.class);
        ELContext elContext = elContextBuilder.defaultContext()
                .withFunction("", "hasRole", m)
                .withFunction("", "hasPermission", p).build();
        return (Boolean) elService.withExpression(expression, Boolean.class).withContext(
                elContext).asValueExpression().eval();
    }
}
