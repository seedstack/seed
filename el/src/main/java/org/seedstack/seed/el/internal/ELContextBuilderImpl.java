/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.el.internal;

import de.odysseus.el.util.SimpleContext;
import org.apache.commons.lang.StringUtils;
import org.seedstack.seed.core.utils.SeedCheckUtils;
import org.seedstack.seed.el.ELContextBuilder;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.StandardELContext;
import java.lang.reflect.Method;

/**
 * Implementation of ELContextBuilder.
 *
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 11/07/2014
 */
class ELContextBuilderImpl implements ELContextBuilder {
    private final ExpressionFactory expressionFactory = ExpressionFactory.newInstance();

    @Override
    public ELPropertyProvider defaultContext() {
        if (ELPlugin.isEL3Present()) {
            return new SubContextBuilderImpl(new StandardELContext(expressionFactory));
        } else if (ELPlugin.isJUELPresent()) {
            return new SubContextBuilderImpl(new SimpleContext());
        } else {
            throw new UnsupportedOperationException("StandardELContext is not supported in this environment (EL level 3+ required)");
        }
    }

    @Override
    public ELPropertyProvider context(ELContext elContext) {
        return new SubContextBuilderImpl(elContext);
    }

    private class SubContextBuilderImpl implements ELContextBuilder.ELPropertyProvider {

        private final ELContext elContext;

        SubContextBuilderImpl(ELContext elContext) {
            this.elContext = elContext;
        }

        @Override
        public ELPropertyProvider withProperty(String name, Object object) {
            SeedCheckUtils.checkIf(StringUtils.isNotBlank(name));
            elContext.getELResolver().setValue(elContext, null, name, object);
            return this;
        }

        @Override
        public ELPropertyProvider withFunction(String prefix, String localName, Method method) {
            SeedCheckUtils.checkIf(StringUtils.isNotBlank(localName));
            if (ELPlugin.isEL3Present()) {
                elContext.getFunctionMapper().mapFunction(prefix, localName, method);
            } else if (ELPlugin.isJUELPresent()) {
                if (elContext instanceof SimpleContext) {
                    ((SimpleContext) elContext).setFunction(prefix, localName, method);
                } else {
                    throw new UnsupportedOperationException("Function mapping is only supported by the default context");
                }
            } else {
                throw new UnsupportedOperationException("Function mapping is not supported in this environment (EL level 3+ required)");
            }
            return this;
        }

        @Override
        public ELContext build() {
            return elContext;
        }
    }
}
