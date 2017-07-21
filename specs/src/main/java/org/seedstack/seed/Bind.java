/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used to declare simple bindings. It must be used on the implementation, optionally referencing
 * another class to be used as injection .
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Bind {
    /**
     * If specified, this class will be used as the binding key, meaning that the implementation will be injectable
     * through this class only (from which the implementation must be assignable). If not specified, the
     * implementation will be bound to itself, meaning that it will be injectable directly.
     *
     * @return the class to be used as injection key.
     */
    Class<?> from() default Object.class;

    /**
     * Only applicable when {@link #from()} is specified. Further qualify the key with an annotation, meaning that the
     * implementation will be injectable through the {@link #from()} class annotated with this annotation.
     *
     * @return the injection qualifier (must be meta-annotated with {@link javax.inject.Qualifier}).
     */
    Class<? extends Annotation> annotated() default Annotation.class;

    /**
     * Only applicable when {@link #from()} is specified. Further qualify the key with a name, meaning that the
     * implementation will be injectable through the {@link #from()} class annotated with {@link javax.inject.Named}
     * with a corresponding value.
     *
     * @return the string qualifying the injection.
     */
    String named() default "";

    /**
     * If true the binding will be defined as an overriding one, meaning that it will override an identical binding
     * already defined. If false, the binding will defined as a normal one.
     *
     * @return if true the binding is an overriding binding, if false a normal binding.
     */
    boolean override() default false;
}
