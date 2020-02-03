/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.authorization;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.aopalliance.intercept.MethodInvocation;
import org.seedstack.seed.core.internal.guice.ProxyUtils;
import org.seedstack.seed.security.AuthorizationException;
import org.seedstack.seed.security.CrudAction;
import org.seedstack.seed.security.RequiresCrudPermissions;
import org.seedstack.seed.security.spi.CrudActionResolver;

public class RequiresCrudPermissionsInterceptor extends AbstractPermissionsInterceptor {
    @Inject
    private Set<CrudActionResolver> resolvers;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        findAnnotation(invocation).ifPresent(rcpAnnotation -> {
            CrudAction action = findVerb(invocation).<AuthorizationException>orElseThrow(() -> {
                throw new AuthorizationException(
                        "Unable to determine CRUD action on method " + invocation.getMethod().toString());
            });
            checkPermissions(
                    invocation.getMethod(),
                    Arrays.stream(rcpAnnotation.value())
                            .map(permission -> String.format("%s:%s", permission, action.getVerb()))
                            .toArray(String[]::new),
                    rcpAnnotation.logical());
        });
        return invocation.proceed();
    }

    private Optional<RequiresCrudPermissions> findAnnotation(MethodInvocation invocation) {
        RequiresCrudPermissions annotation = invocation.getMethod().getAnnotation(RequiresCrudPermissions.class);
        if (annotation == null) {
            annotation = ProxyUtils.cleanProxy(invocation.getThis().getClass()).getAnnotation(
                    RequiresCrudPermissions.class);
        }
        return Optional.ofNullable(annotation);
    }

    private Optional<CrudAction> findVerb(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        // returns the result of the first resolver that gives a valid action
        return resolvers.stream()
                .map(x -> x.resolve(method))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }
}
