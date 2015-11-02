/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.el.internal;

import org.seedstack.seed.core.utils.SeedCheckUtils;
import org.seedstack.seed.el.ELContextBuilder;
import de.odysseus.el.util.SimpleContext;
import org.apache.commons.lang.StringUtils;

import javax.el.ELContext;
import java.lang.reflect.Method;

/**
 * Implementation of ELContextBuilder.
 *
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 11/07/2014
 */
public class ELContextBuilderImpl implements ELContextBuilder {

    @Override
    public ELPropertyProvider defaultContext() {
        return new SubContextBuilderImpl(new SimpleContext());
    }

    @Override
    public ELPropertyProvider context(ELContext elContext) {
        return new SubContextBuilderImpl(elContext);
    }

    class SubContextBuilderImpl implements ELContextBuilder.ELPropertyProvider {

        private ELContext elContext;

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
            if (elContext instanceof SimpleContext) {
                ((SimpleContext) elContext).setFunction(prefix, localName, method);
            } else {
                throw new UnsupportedOperationException("This method is only supported by the default context.");
            }
            return this;
        }

        @Override
        public ELContext build() {
            return elContext;
        }
    }
}
