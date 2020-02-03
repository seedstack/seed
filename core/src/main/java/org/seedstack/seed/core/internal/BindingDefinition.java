/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.inject.Binder;
import com.google.inject.binder.AnnotatedBindingBuilder;
import org.seedstack.seed.SeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BindingDefinition<T> extends Bindable<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BindingDefinition.class);
    private final Class<T> from;

    BindingDefinition(Class<? extends T> target, Class<T> from) {
        super(target);
        this.from = from;
    }

    void apply(Binder binder) {
        if (from != null) {
            AnnotatedBindingBuilder<T> bind = binder.bind(from);
            if (!from.isAssignableFrom(target)) {
                throw SeedException.createNew(CoreErrorCode.INVALID_BINDING)
                        .put("key", from)
                        .put("target", target);
            }
            if (qualifier != null) {
                LOGGER.debug("Binding {} annotated with {} to {}", from.getName(), qualifier, target.getName());
                bind.annotatedWith(qualifier).to(target);
            } else {
                LOGGER.debug("Binding {} to {}", from.getName(), target.getName());
                bind.to(target);
            }
        } else {
            if (qualifier != null) {
                LOGGER.debug("Binding {} annotated with {} to itself", target.getName(), qualifier);
                binder.bind(target).annotatedWith(qualifier);
            } else {
                LOGGER.debug("Binding {} to itself", target.getName());
                binder.bind(target);
            }
        }
    }
}
