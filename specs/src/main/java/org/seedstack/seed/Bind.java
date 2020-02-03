/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation makes the class on which it is applied, injectable. In the basic case, the class will be
 * injectable with its own type:
 *
 * <pre>
 * {@literal @}Bind
 *  public class SomeImplementation {...}
 *
 * {@literal @}Inject
 *  SomeImplementation someImplementation;
 * </pre>
 *
 * <p>
 * If an injection class is specified with {@link #from()}, the implementation will be injectable with the specified
 * type instead:
 * </p>
 *
 * <pre>
 * {@literal @}Bind(from = SomeInterface.class)
 *  public class SomeImplementation implements SomeInterface {...}
 *
 * {@literal @}Inject
 *  SomeInterface someInterface;
 * </pre>
 *
 * <p>
 * <strong>The {@link Bind} annotation allows to override any interface-based SeedStack binding.</strong> To do this,
 * create a custom implementation of the SeedStack interface you want to customize (this is the type you use at the
 * injection point) and set the {@link Bind#override()} boolean to true. Your custom implementation will replace the
 * SeedStack one.
 * </p>
 * <p></p>
 * <p>
 * When a qualifier annotation is present on the implementation class, it is used to make the injection point more
 * specific:
 * </p>
 *
 * <pre>
 * {@literal @}Qualifier
 * {@literal @}Retention(RetentionPolicy.RUNTIME)
 *  public interface {@literal @}SomeQualifier {...}
 *
 * {@literal @}Bind(from = SomeInterface.class)
 * {@literal @}SomeQualifier
 *  public class SomeImplementation implements SomeInterface {...}
 *
 * {@literal @}Inject
 * {@literal @}SomeQualifier
 *  SomeInterface someInterface;
 * </pre>
 *
 * <p>When having multiple implementations of the same interface, using a different qualifier on each implementation
 * allows to create multiple bindings. You can then choose the implementation by specifying the corresponding qualifier
 * at injection point.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Bind {
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
