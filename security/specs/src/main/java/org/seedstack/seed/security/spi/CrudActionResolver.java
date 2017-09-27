/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.spi;

import java.lang.reflect.Method;
import java.util.Optional;
import org.seedstack.seed.security.CrudAction;

/**
 * A class implementing {@link CrudActionResolver} provides logic to resolve the {@link CrudAction} that is associated
 * to a particular method. For instance, a JAX-RS resolver could use JAX-RS annotations to determine the ongoing CRUD
 * action. Another example would be a Servlet resolver which can use the method signature of an HttpServlet to determine
 * the corresponding action.
 *
 * <p>
 * Classes implementing this interface can be annotated with {@link javax.annotation.Priority} to define an absolute
 * order among them.
 * </p>
 */
public interface CrudActionResolver {
    /**
     * Resolves a {@link CrudAction} from the specified method object.
     *
     * @param method the method object.
     * @return an optionally resolved {@link CrudAction}.
     */
    Optional<CrudAction> resolve(Method method);
}