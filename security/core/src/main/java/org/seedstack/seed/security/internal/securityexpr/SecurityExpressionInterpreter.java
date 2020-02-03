/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.securityexpr;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.el.ELContext;
import javax.inject.Inject;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.el.ELContextBuilder;
import org.seedstack.seed.el.ELService;
import org.seedstack.seed.security.internal.SecurityErrorCode;

/**
 * The Security Expression Interpreter has the responsibility to interpret any form of security expression.
 */
public class SecurityExpressionInterpreter {
    @Inject
    private ELService elService;
    @Inject
    private ELContextBuilder elContextBuilder;

    /**
     * This methods will interpret any security expression. It will handle it
     * accordingly in function of its type.
     * <p>
     * For now, Boolean will be interpreted as Boolean and String will be
     * interpreted by the {@link ELService} as an Expression Language from the JSR 341.
     *
     * @param securityExpression the security expression.
     * @return True if the security expression interpretation is secured.
     */
    public Boolean interpret(Object securityExpression) {
        Boolean interpretation = Boolean.FALSE;

        if (securityExpression instanceof Boolean) {
            interpretation = Boolean.class.cast(securityExpression);
        } else if (securityExpression instanceof String) {
            interpretation = expression((String) securityExpression);
        }

        return interpretation;
    }

    private Boolean expression(String expression) {
        ELContextBuilder.ELPropertyProvider elContextProvider = elContextBuilder.defaultContext();

        for (Map.Entry<String, Method> entry : getMethods().entrySet()) {
            elContextProvider.withFunction("", entry.getKey(), entry.getValue());
        }

        ELContext elContext = elContextProvider.build();
        return (Boolean) elService.withExpression(expression, Boolean.class).withContext(
                elContext).asValueExpression().eval();
    }

    private Map<String, Method> getMethods() {
        Map<String, Method> availableMethods = new HashMap<>();
        try {
            availableMethods.put("hasRole", SecurityExpressionUtils.class.getDeclaredMethod("hasRole", String.class));
            availableMethods.put("hasRoleOn",
                    SecurityExpressionUtils.class.getDeclaredMethod("hasRoleOn", String.class, String.class));
            availableMethods.put("hasPermission",
                    SecurityExpressionUtils.class.getDeclaredMethod("hasPermission", String.class));
            availableMethods.put("hasPermissionOn",
                    SecurityExpressionUtils.class.getDeclaredMethod("hasPermissionOn", String.class, String.class));
        } catch (NoSuchMethodException e) {
            throw SeedException.wrap(e, SecurityErrorCode.UNEXPECTED_ERROR);
        }
        return availableMethods;
    }
}
