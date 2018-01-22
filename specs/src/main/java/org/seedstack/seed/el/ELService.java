/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.el;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import org.seedstack.seed.SeedException;

/**
 * The ELService provides a DSL to facilitate the evaluation of expression language.
 */
public interface ELService {

    /**
     * Sets the expression language to evaluate and specifies the expected return type.
     *
     * @param el         the expression language to evaluate
     * @param returnType the expected return type
     * @return an ELContextProvider
     * @throws SeedException if el is blank or returnType is null
     */
    ELContextProvider withExpression(String el, Class returnType);

    /**
     * Sets a ValueExpression to evaluate.
     *
     * @param valueExpression the value expression to set
     * @return ValueExpressionProvider
     * @throws SeedException if the valueExpression is null
     */
    ValueExpressionProvider withValueExpression(ValueExpression valueExpression);

    /**
     * Sets a MethodExpression to invoke.
     *
     * @param methodExpression the method expression to set
     * @return MethodExpressionProvider
     * @throws SeedException if the methodExpression is null
     */
    MethodExpressionProvider withMethodExpression(MethodExpression methodExpression);

    /**
     * This interface provides methods to add ELContext for EL evaluation.
     */
    interface ELContextProvider {

        /**
         * Use a specific context to use.
         *
         * @param elContext the context to use.
         * @return ELExpressionProvider
         */
        ELExpressionProvider withContext(ELContext elContext);

        /**
         * Use the default context.
         *
         * @return ELExpressionProvider
         */
        ELExpressionProvider withDefaultContext();
    }

    /**
     * This interface provides methods to add properties for the EL evaluation.
     */
    interface ELExpressionProvider {

        /**
         * Gets a {@link javax.el.ValueExpression}.
         *
         * @return ValueExpressionProvider
         */
        ValueExpressionProvider asValueExpression();

        /**
         * Gets a {@link javax.el.MethodExpression}.
         *
         * @param expectedParamTypes the argument types of the method specified in the EL
         * @return MethodExpressionProvider
         */
        MethodExpressionProvider asMethodExpression(Class<?>[] expectedParamTypes);

    }

    /**
     * This interface provides methods to evaluate a value expression.
     */
    interface ValueExpressionProvider {

        /**
         * Evaluates the EL.
         *
         * @return EL result
         */
        Object eval();

        /**
         * Gets the ValueExpression.
         *
         * @return ValueExpression
         */
        ValueExpression valueExpression();
    }

    /**
     * This interface provides methods to evaluate a method expression.
     */
    interface MethodExpressionProvider {

        /**
         * Invokes the method specified in the EL with the provided arguments.
         *
         * @param args arguments to pass to the method
         * @return EL result
         */
        Object invoke(Object[] args);

        /**
         * Gets the MethodExpressionProvider.
         *
         * @return MethodExpressionProvider
         */
        MethodExpression methodExpression();
    }
}
