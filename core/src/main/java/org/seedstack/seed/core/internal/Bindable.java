/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Binder;
import java.lang.annotation.Annotation;
import java.util.Optional;
import javax.inject.Qualifier;
import org.seedstack.shed.reflect.AnnotationPredicates;
import org.seedstack.shed.reflect.Annotations;

abstract class Bindable<T> {
    final Class<? extends T> target;
    final Annotation qualifier;

    Bindable(Class<? extends T> target) {
        this.target = checkNotNull(target, "Binding target should not be null");
        this.qualifier = findQualifier(this.target).orElse(null);
    }

    abstract void apply(Binder binder);

    private Optional<Annotation> findQualifier(Class<? extends T> target) {
        return Annotations.on(target)
                .traversingSuperclasses()
                .traversingInterfaces()
                .findAll()
                .filter(AnnotationPredicates.annotationAnnotatedWith(Qualifier.class, false))
                .findFirst();
    }
}
