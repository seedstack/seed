/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation makes the class on which it is applied, injectable during testing only (i.e. when the testing plugin
 * is present, typically in the test classpath). It is the testing-only equivalent of the {@link org.seedstack.seed.Bind}
 * annotation.
 *
 * @see org.seedstack.seed.Bind
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ITBind {
    /**
     * If specified, this class will be used as the binding key, meaning that the implementation will be injectable
     * through this class only (from which the implementation must be assignable). If not specified, the
     * implementation will be bound to itself, meaning that it will be injectable directly. When this parameter is
     * specified, a qualifier annotation can optionally be applied on the implementation to define a qualifier key.
     *
     * @return the class to be used as injection key.
     */
    Class<?> from() default Object.class;

    /**
     * If true the binding will be defined as an overriding one, meaning that it will override an identical binding
     * already defined. If false, the binding will defined as a normal one.
     *
     * @return if true the binding is an overriding binding, if false a normal binding.
     */
    boolean override() default false;
}
