/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.authorization;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.security.Logical;
import org.seedstack.seed.security.RequiresRoles;
import org.seedstack.seed.security.AuthorizationException;

import java.lang.annotation.Annotation;

/**
 * Interceptor for annotation RequiresRoles
 * 
 * @author yves.dautremay@mpsa.com
 * 
 */
public class RequiresRolesInterceptor implements MethodInterceptor {

    private SecuritySupport securitySupport;

    /**
     * Constructor
     * 
     * @param securitySupport
     *            the security support
     */
    public RequiresRolesInterceptor(SecuritySupport securitySupport) {
        this.securitySupport = securitySupport;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Annotation annotation = findAnnotation(invocation);
        if (annotation == null) {
            return invocation.proceed();
        }
        RequiresRoles rrAnnotation = (RequiresRoles) annotation;
        String[] roles = rrAnnotation.value();
        if (roles.length == 1) {
            securitySupport.checkRole(roles[0]);
            return invocation.proceed();
        } else if (Logical.OR.equals(rrAnnotation.logical())) {
            boolean hasAtLeastOneRole = false;
            for (String role : roles) {
                if (securitySupport.hasRole(role)) {
                    hasAtLeastOneRole = true;
                    break;
                }
            }
            if (!hasAtLeastOneRole) {
                throw new AuthorizationException("User does not have any of the roles to access method " + invocation.getMethod().toString());
            }
        } else {
            // Otherwise rrAnnotation.logical() is by default considered as Logical.AND
            securitySupport.checkRoles(roles);
        }
        return invocation.proceed();
    }

    private Annotation findAnnotation(MethodInvocation invocation) {
        Annotation annotation = invocation.getMethod().getAnnotation(RequiresRoles.class);
        if (annotation == null) {
            annotation = invocation.getThis().getClass().getAnnotation(RequiresRoles.class);
        }
        return annotation;
    }
}
