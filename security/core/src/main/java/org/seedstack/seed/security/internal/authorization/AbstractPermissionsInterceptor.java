/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal.authorization;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.aopalliance.intercept.MethodInterceptor;
import org.seedstack.seed.security.AuthorizationException;
import org.seedstack.seed.security.Logical;

public abstract class AbstractPermissionsInterceptor extends AbstractInterceptor implements MethodInterceptor {
    protected void checkPermissions(Method method, String[] perms, Logical logical) {
        if (perms.length == 1) {
            checkPermission(perms[0]);
        } else {
            boolean isAllowed = hasPermissions(perms, logical);
            if (!isAllowed) {
                if (Logical.OR.equals(logical)) {
                    throw new AuthorizationException(
                            "Subject does not have any of the permissions to access method " + method.toString());
                } else {
                    throw new AuthorizationException("Subject doesn't have permissions " + Arrays.toString(perms));
                }
            }
        }
    }
}
