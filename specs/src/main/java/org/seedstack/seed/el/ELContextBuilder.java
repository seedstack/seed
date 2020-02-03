/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.el;

import java.lang.reflect.Method;
import javax.el.ELContext;
import org.seedstack.seed.SeedException;

/**
 * ELContextBuilder provides a DSL to build ELContext instances.
 */
public interface ELContextBuilder {

    /**
     * Initializes the context with default context.
     *
     * @return ELPropertyProvider
     */
    ELPropertyProvider defaultContext();

    /**
     * Sets a custom {@link javax.el.ELContext}.
     *
     * @param elContext custom ELContext
     * @return ELPropertyProvider
     * @throws SeedException if the context is null
     */
    ELPropertyProvider context(ELContext elContext);

    /**
     * Grammar to add properties and methods to an ELContext.
     */
    interface ELPropertyProvider {

        /**
         * @return the context used by the service.
         */
        ELContext build();

        /**
         * Adds property to the EL.
         *
         * @param name   the name to use in the EL
         * @param object the associated object
         * @return ELPropertyProvider
         * @throws SeedException if the name is blank
         */
        ELPropertyProvider withProperty(String name, Object object);

        /**
         * Adds a function which will be available in the EL. For instance:
         * <code>
         * .withFunction("maths", "max", Math.class.getMethod("max", double.class, double.class))
         * </code>
         * <p>Provides the function max in the EL usable as follow:</p>
         * <code>
         * "${maths:max(1,2)}"
         * </code>
         *
         * @param prefix    the method prefix
         * @param localName the method name
         * @param method    the actual method to invoke
         * @return ELPropertyProvider
         * @throws SeedException if the localName is blank
         */
        ELPropertyProvider withFunction(String prefix, String localName, Method method);

    }
}
