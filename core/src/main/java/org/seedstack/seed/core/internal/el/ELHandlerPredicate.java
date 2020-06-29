/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.el;

import static org.seedstack.shed.reflect.ClassPredicates.classImplements;
import static org.seedstack.shed.reflect.ClassPredicates.classModifierIs;

import java.lang.reflect.Modifier;
import java.util.function.Predicate;
import org.seedstack.seed.el.spi.ELHandler;

class ELHandlerPredicate implements Predicate<Class<?>> {
    static ELHandlerPredicate INSTANCE = new ELHandlerPredicate();

    private ELHandlerPredicate() {
        // no instantiation allowed
    }

    @Override
    public boolean test(Class<?> candidate) {
        return classImplements(ELHandler.class)
                .and(classModifierIs(Modifier.ABSTRACT).negate())
                .test(candidate);
    }
}
