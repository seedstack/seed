/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
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
import javax.inject.Provider;

/**
 * This annotation creates an injection binding when applied on a JSR-330 {@link javax.inject.Provider}. It is
 * comparable to the {@link Bind} annotation but allow to specify the creation logic of the instance in the
 * {@link Provider#get()} method. A provider is itself injectable so you can use any required dependency during the
 * creation of the provided instance.
 *
 * <pre>
 * {@literal @}Provide
 *  public class Provider{@literal &lt;}SomeClass{@literal &gt;} {
 *     {@literal @}Inject
 *      private SomeDependency someDependency;
 *
 *      public SomeInterface get() {
 *          return new SomeClass(someDependency);
 *      }
 *  }
 *
 * {@literal @}Inject
 *  SomeClass someClassInstance;
 * </pre>
 *
 * <p>
 * <strong>The {@link Provide} annotation allows to override any existing SeedStack binding.</strong> To do this,
 * create a {@link Provider} producing the same type as the SeedStack binding you want to override (this is the type
 * you use at the injection point) and set the {@link Provide#override()} boolean to true. The instance produced by your
 * provider will replace the SeedStack one.
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
public @interface Provide {
    /**
     * If true the binding will be defined as an overriding one, meaning that it will override an identical binding
     * already defined. If false, the binding will defined as a normal one.
     *
     * @return if true the binding is an overriding binding, if false a normal binding.
     */
    boolean override() default false;
}
