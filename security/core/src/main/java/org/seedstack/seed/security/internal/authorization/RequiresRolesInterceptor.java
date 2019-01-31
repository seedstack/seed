/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.authorization;

import java.util.Arrays;
import java.util.Optional;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.seedstack.seed.core.internal.guice.ProxyUtils;
import org.seedstack.seed.security.AuthorizationException;
import org.seedstack.seed.security.Logical;
import org.seedstack.seed.security.RequiresRoles;

/**
 * Interceptor for annotation RequiresRoles
 */
public class RequiresRolesInterceptor extends AbstractInterceptor implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Optional<RequiresRoles> annotation = findAnnotation(invocation);
        if (annotation.isPresent()) {
            RequiresRoles rrAnnotation = annotation.get();
            String[] roles = rrAnnotation.value();
            if (roles.length == 1) {
                checkRole(roles[0]);
            } else {
                boolean isAllowed = hasRoles(roles, rrAnnotation.logical());
                if (!isAllowed) {
                    if (Logical.OR.equals(rrAnnotation.logical())) {
                        throw new AuthorizationException(
                                "User does not have any of the roles to access method " + invocation.getMethod()
                                        .toString());
                    } else {
                        throw new AuthorizationException("Subject doesn't have roles " + Arrays.toString(roles));
                    }
                }
            }
        }
        return invocation.proceed();
    }

    private Optional<RequiresRoles> findAnnotation(MethodInvocation invocation) {
        RequiresRoles annotation = invocation.getMethod().getAnnotation(RequiresRoles.class);
        if (annotation == null) {
            annotation = ProxyUtils.cleanProxy(invocation.getThis().getClass()).getAnnotation(RequiresRoles.class);
        }
        return Optional.ofNullable(annotation);
    }
}
