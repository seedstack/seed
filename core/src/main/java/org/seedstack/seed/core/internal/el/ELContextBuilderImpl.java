/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.el;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Strings;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.inject.Inject;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.el.ELContextBuilder;

/**
 * Implementation of ELContextBuilder.
 *
 * *
 */
class ELContextBuilderImpl implements ELContextBuilder {
    @Inject
    private ExpressionFactory expressionFactory;

    static ELContext createDefaultELContext(ExpressionFactory expressionFactory) {
        if (ELPlugin.EL_3_CONTEXT_CLASS != null) {
            try {
                return ELPlugin.EL_3_CONTEXT_CLASS.getConstructor(ExpressionFactory.class).newInstance(
                        expressionFactory);
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                    InstantiationException e) {
                throw new RuntimeException("Unable to instantiate StandardELContext", e);
            }
        } else if (ELPlugin.JUEL_CONTEXT_CLASS != null) {
            try {
                return ELPlugin.JUEL_CONTEXT_CLASS.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException("Unable to instantiate JUEL SimpleContext", e);
            }
        } else {
            throw new UnsupportedOperationException(
                    "StandardELContext is not supported in this environment (EL level 3+ required)");
        }
    }

    @Override
    public ELPropertyProvider defaultContext() {
        return new SubContextBuilderImpl(createDefaultELContext(expressionFactory));
    }

    @Override
    public ELPropertyProvider context(ELContext elContext) {
        return new SubContextBuilderImpl(elContext);
    }

    private static class SubContextBuilderImpl implements ELContextBuilder.ELPropertyProvider {
        private final ELContext elContext;

        SubContextBuilderImpl(ELContext elContext) {
            this.elContext = elContext;
        }

        @Override
        public ELPropertyProvider withProperty(String name, Object object) {
            checkArgument(!Strings.isNullOrEmpty(name), "A property name is required");
            elContext.getELResolver().setValue(elContext, null, name, object);
            return this;
        }

        @Override
        public ELPropertyProvider withFunction(String prefix, String localName, Method method) {
            checkArgument(!Strings.isNullOrEmpty(localName), "A function local name is required");
            if (ELPlugin.isLevel3()) {
                elContext.getFunctionMapper().mapFunction(prefix, localName, method);
            } else if (ELPlugin.JUEL_CONTEXT_CLASS != null) {
                if (ELPlugin.JUEL_CONTEXT_CLASS.isAssignableFrom(elContext.getClass())) {
                    try {
                        ELPlugin.JUEL_CONTEXT_CLASS.getMethod("setFunction", String.class, String.class,
                                Method.class).invoke(elContext, prefix, localName, method);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        throw SeedException.wrap(e, ExpressionLanguageErrorCode.UNEXPECTED_EXCEPTION);
                    }
                } else {
                    throw new UnsupportedOperationException(
                            "At EL level 2, function mapping is only supported by JUEL SimpleContext");
                }
            } else {
                throw new UnsupportedOperationException(
                        "Function mapping is not supported in this environment (EL level 3+ or JUEL required)");
            }
            return this;
        }

        @Override
        public ELContext build() {
            return elContext;
        }
    }
}
