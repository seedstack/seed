/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal.authorization;

import java.util.Optional;
import org.aopalliance.intercept.MethodInvocation;
import org.seedstack.seed.core.internal.guice.ProxyUtils;
import org.seedstack.seed.security.RequiresPermissions;

/**
 * Interceptor for the annotation RequiresPermissions
 */
public class RequiresPermissionsInterceptor extends AbstractPermissionsInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        findAnnotation(invocation).ifPresent(rpAnnotation -> {
            checkPermissions(invocation.getMethod(), rpAnnotation.value(), rpAnnotation.logical());
        });
        return invocation.proceed();
    }

    private Optional<RequiresPermissions> findAnnotation(MethodInvocation invocation) {
        RequiresPermissions annotation = invocation.getMethod().getAnnotation(RequiresPermissions.class);
        if (annotation == null) {
            annotation = ProxyUtils.cleanProxy(invocation.getThis().getClass()).getAnnotation(
                    RequiresPermissions.class);
        }
        return Optional.ofNullable(annotation);
    }
}
