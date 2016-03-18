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
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.utils.SeedCheckUtils;
import org.seedstack.seed.el.ELService;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.PropertyNotFoundException;
import javax.el.StandardELContext;
import javax.el.ValueExpression;

/**
 * Implementation of ELService.
 *
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 02/07/2014
 */
class ELServiceInternal implements ELService {

    @Override
    public ELContextProvider withExpression(String el, Class returnType) {
        SeedCheckUtils.checkIf(StringUtils.isNotBlank(el));
        SeedCheckUtils.checkIfNotNull(returnType);
        return new ELInstance(el, returnType);
    }

    @Override
    public ValueExpressionProvider withValueExpression(ValueExpression valueExpression) {
        SeedCheckUtils.checkIfNotNull(valueExpression);
        ELInstance elInstance = new ELInstance();
        elInstance.setValueExpression(valueExpression);
        return elInstance;
    }

    @Override
    public MethodExpressionProvider withMethodExpression(MethodExpression methodExpression) {
        SeedCheckUtils.checkIfNotNull(methodExpression);
        ELInstance elInstance = new ELInstance();
        elInstance.setMethodExpression(methodExpression);
        return elInstance;
    }

    private class ELInstance implements ELContextProvider, ELExpressionProvider, ELService.MethodExpressionProvider, ELService.ValueExpressionProvider {

        private ExpressionFactory expressionFactory = ExpressionFactory.newInstance();

        private MethodExpression methodExpression;

        private ValueExpression valueExpression;

        private Class returnType;

        private ELContext context;

        private String el;

        ELInstance() {
        }

        ELInstance(String el, Class returnType) {
            this.el = el;
            this.returnType = returnType;
        }

        @Override
        public ELExpressionProvider withContext(ELContext elContext) {
            SeedCheckUtils.checkIfNotNull(elContext);
            context = elContext;
            return this;
        }

        @Override
        public ELExpressionProvider withDefaultContext() {
            if (ELPlugin.isEL3Present()) {
                context = new StandardELContext(expressionFactory);
                return this;
            } else if (ELPlugin.isJUELPresent()) {
                context = new SimpleContext();
                return this;
            } else {
                throw new UnsupportedOperationException("StandardELContext is not supported in this environment (EL level 3+ required)");
            }
        }

        @Override
        public ValueExpression valueExpression() {
            return valueExpression;
        }

        @Override
        public ValueExpressionProvider asValueExpression() {
            valueExpression = expressionFactory.createValueExpression(context, el, returnType);
            return this;
        }

        @Override
        public MethodExpression methodExpression() {
            return methodExpression;
        }

        @Override
        public MethodExpressionProvider asMethodExpression(Class<?>[] expectedParamTypes) {
            methodExpression = expressionFactory.createMethodExpression(context, el, returnType, expectedParamTypes);
            return this;
        }

        @Override
        public Object eval() {
            Object value;
            try {
                value = valueExpression.getValue(context);
            } catch (PropertyNotFoundException e) {
                throw SeedException.wrap(e, ELErrorCode.PROPERTY_NOT_FOUND).put("el", el);
            } catch (ELException e) {
                throw SeedException.wrap(e, ELErrorCode.EL_EXCEPTION).put("el", el);
            }
            return value;
        }

        @Override
        public Object invoke(Object[] args) {
            Object value;
            try {
                value = methodExpression.invoke(context, args);
            } catch (PropertyNotFoundException e) {
                throw SeedException.wrap(e, ELErrorCode.PROPERTY_NOT_FOUND).put("el", el);
            } catch (ELException e) {
                throw SeedException.wrap(e, ELErrorCode.EL_EXCEPTION).put("el", el);
            }
            return value;
        }

        void setMethodExpression(MethodExpression methodExpression) {
            this.methodExpression = methodExpression;
        }

        void setValueExpression(ValueExpression valueExpression) {
            this.valueExpression = valueExpression;
        }
    }
}
