/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.guice;

/**
 * This interface is used as a generic Guice assisted factory for create generic type.
 *
 * @param <T> the produced type
 */
public interface GenericGuiceFactory<T> {

    /**
     * Create an instance of T, with its resolved type variables.
     *
     * @param typeVariableClasses the resolved type variables classes. But it could also be tuple of classes.
     * @return the produced class
     */
    T createResolvedInstance(Object[] typeVariableClasses);
}
