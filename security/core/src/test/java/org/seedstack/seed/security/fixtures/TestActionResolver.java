/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.fixtures;

import java.lang.reflect.Method;
import java.util.Optional;
import org.seedstack.seed.security.CrudAction;
import org.seedstack.seed.security.fixtures.annotations.READ;
import org.seedstack.seed.security.fixtures.annotations.CREATE;
import org.seedstack.seed.security.fixtures.annotations.DELETE;
import org.seedstack.seed.security.fixtures.annotations.UPDATE;
import org.seedstack.seed.security.spi.CrudActionResolver;

public class TestActionResolver implements CrudActionResolver {
    @Override
    public Optional<CrudAction> resolve(Method method) {

        if (method.getAnnotation(CREATE.class) != null) {
            return Optional.of(CrudAction.CREATE);
        }

        if (method.getAnnotation(READ.class) != null) {
            return Optional.of(CrudAction.READ);
        }
        if (method.getAnnotation(UPDATE.class) != null) {
            return Optional.of(CrudAction.UPDATE);
        }
        if (method.getAnnotation(DELETE.class) != null) {
            return Optional.of(CrudAction.DELETE);
        }
        return Optional.empty();
    }
}
