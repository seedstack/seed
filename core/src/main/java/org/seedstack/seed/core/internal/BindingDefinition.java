/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.inject.Binder;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.name.Names;
import org.seedstack.seed.SeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;

import static com.google.common.base.Preconditions.checkNotNull;

public class BindingDefinition<E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BindingDefinition.class);
    private final Class<E> key;
    private final Class<? extends Annotation> qualifier;
    private final String name;
    private final Class<? extends E> target;

    public BindingDefinition(Class<? extends E> target) {
        this(null, null, null, target);
    }

    public BindingDefinition(Class<E> key, Class<? extends E> target) {
        this(key, null, null, target);
    }

    public BindingDefinition(Class<E> key, String qualifier, Class<? extends E> target) {
        this(key, null, qualifier, target);
    }

    public BindingDefinition(Class<E> key, Class<? extends Annotation> qualifier, Class<? extends E> target) {
        this(key, qualifier, null, target);
    }

    public BindingDefinition(Class<E> key, Class<? extends Annotation> qualifier, String name, Class<? extends E> target) {
        this.key = key;
        this.qualifier = qualifier;
        this.name = name;
        this.target = checkNotNull(target, "Binding target should not be null");
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
                LOGGER.trace("Binding {} annotated with @{} to {}", key.getName(), qualifier.getName(), target.getName());
                bind.annotatedWith(qualifier).to(getExtendingClass(target));
            } else if (name != null) {
                LOGGER.trace("Binding {} annotated with @Named(value={}) to {}", key.getName(), name, target.getName());
                bind.annotatedWith(Names.named(name)).to(getExtendingClass(target));
            } else {
                LOGGER.trace("Binding {} to {}", key.getName(), target.getName());
                bind.to(getExtendingClass(target));
            }
        } else {
            LOGGER.trace("Binding {} to itself", target.getName());
            binder.bind(target);
        }
    }

    @SuppressWarnings("unchecked")
    private <C extends Class<?>> C getExtendingClass(Class<?> aClass) {
        return (C) aClass;
    }
}
