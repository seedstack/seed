/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.lifecycle;

import com.google.inject.Binding;
import com.google.inject.matcher.AbstractMatcher;
import org.seedstack.shed.reflect.Classes;

import javax.annotation.PostConstruct;

import static org.seedstack.shed.reflect.AnnotationPredicates.elementAnnotatedWith;

class ConstructionMatcher extends AbstractMatcher<Binding<?>> {
    @Override
    public boolean matches(Binding<?> binding) {
        return hasPostConstruct(binding.getKey().getTypeLiteral().getRawType());
    }

    private boolean hasPostConstruct(Class<?> rawType) {
        return Classes.from(rawType)
                .traversingInterfaces()
                .traversingSuperclasses()
                .methods()
                .anyMatch(elementAnnotatedWith(PostConstruct.class, true));
    }
}
