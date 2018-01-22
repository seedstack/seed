/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.seedstack.seed.security.CrudAction;
import org.seedstack.seed.security.spi.CrudActionResolver;

class ServletCrudActionResolver implements CrudActionResolver {
    private static final Class<?>[] PARAMETER_TYPES = {HttpServletRequest.class, HttpServletResponse.class};

    @Override
    public Optional<CrudAction> resolve(Method method) {
        if (HttpServlet.class.isAssignableFrom(method.getDeclaringClass()) && Arrays.equals(method.getParameterTypes(),
                PARAMETER_TYPES)) {
            switch (method.getName()) {
                case "doDelete":
                    return Optional.of(CrudAction.DELETE);
                case "doGet":
                    return Optional.of(CrudAction.READ);
                case "doHead":
                    return Optional.of(CrudAction.READ);
                case "doOptions":
                    return Optional.of(CrudAction.READ);
                case "doPost":
                    return Optional.of(CrudAction.CREATE);
                case "doPut":
                    return Optional.of(CrudAction.UPDATE);
                case "doTrace":
                    return Optional.of(CrudAction.READ);
                default:
                    return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}
