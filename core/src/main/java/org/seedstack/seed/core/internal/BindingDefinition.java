/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Binder;
import com.google.inject.binder.AnnotatedBindingBuilder;
import java.lang.annotation.Annotation;
import java.util.Optional;
import javax.inject.Qualifier;
import org.seedstack.seed.SeedException;
import org.seedstack.shed.reflect.AnnotationPredicates;
import org.seedstack.shed.reflect.Annotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingDefinition<E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BindingDefinition.class);
    private final Class<E> key;
    private final Class<? extends E> target;
    private final Annotation qualifier;

    public BindingDefinition(Class<? extends E> target) {
        this(target, null);
    }

    public BindingDefinition(Class<? extends E> target, Class<E> fromKey) {
        this.target = checkNotNull(target, "Binding target should not be null");
        this.key = fromKey;
        this.qualifier = findQualifier(this.target).orElse(null);
    }

    public void apply(Binder binder) {
        if (key != null) {
            AnnotatedBindingBuilder<?> bind = binder.bind(key);
            if (!key.isAssignableFrom(target)) {
                throw SeedException.createNew(CoreErrorCode.INVALID_BINDING)
                        .put("key", key)
                        .put("target", target);
            }
            if (qualifier != null) {
                LOGGER.trace("Binding {} annotated with {} to {}", key.getName(), qualifier, target.getName());
                bind.annotatedWith(qualifier).to(getExtendingClass(target));
            } else {
                LOGGER.trace("Binding {} to {}", key.getName(), target.getName());
                bind.to(getExtendingClass(target));
            }
        } else {
            LOGGER.trace("Binding {} to itself", target.getName());
            binder.bind(target);
        }
    }

    private Optional<Annotation> findQualifier(Class<? extends E> target) {
        return Annotations.on(target)
                .traversingSuperclasses()
                .traversingInterfaces()
                .findAll()
                .filter(AnnotationPredicates.annotationAnnotatedWith(Qualifier.class, false))
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    private <C extends Class<?>> C getExtendingClass(Class<?> aClass) {
        return (C) aClass;
    }
}
