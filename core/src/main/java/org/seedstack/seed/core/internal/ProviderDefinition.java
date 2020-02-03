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
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.inject.Provider;
import net.jodah.typetools.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProviderDefinition<T, P extends Provider<T>> extends Bindable<P> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderDefinition.class);
    private final Type from;

    ProviderDefinition(Class<P> provider) {
        super(provider);
        Type type = checkNotNull(TypeResolver.resolveGenericType(Provider.class, provider),
                "Unable to resolve generic type of provider " + provider.getName());
        if (type instanceof ParameterizedType) {
            from = ((ParameterizedType) type).getActualTypeArguments()[0];
        } else {
            throw new IllegalArgumentException("A generic type is required for provider " + provider.getName());
        }
    }

    @SuppressWarnings("unchecked")
    public void apply(Binder binder) {
        AnnotatedBindingBuilder<T> bind = binder.bind((TypeLiteral<T>) TypeLiteral.get(from));
        if (qualifier != null) {
            LOGGER.debug("Binding {} annotated with {} to provider {}",
                    from.getTypeName(),
                    qualifier,
                    target.getName());
            bind.annotatedWith(qualifier).toProvider(target);
        } else {
            LOGGER.debug("Binding {} to provider {}", from.getTypeName(), target.getName());
            bind.toProvider(target);
        }
    }
}
