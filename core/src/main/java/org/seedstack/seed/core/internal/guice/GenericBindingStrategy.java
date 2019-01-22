/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.guice;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.util.Types;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * GenericBindingStrategy resolves bindings for generic classes to implementations with unresolved type variables.
 * <p>
 * For instance it is possible to bind the following classes:
 * </p>
 * <pre>
 * class MyClass&lt;I, J&gt; { }
 *
 * class MyImplClass&lt;I,J&gt; extends MyClass { }
 * </pre>
 * For all the possible type variables (for instance for all the aggregate with their key).
 * <pre>
 * Collection&lt;Class&lt;?&gt;[]&gt; constructorParams = Lists.newArrayList();
 * constructorParams.add(new Object[]{MyAggregate1.class, MyKey1.class});
 * constructorParams.add(new Object[]{MyAggregate2.class, MyKey2.class});
 *
 * GenericBindingStrategy bindingStrategy = new GenericBindingStrategy(MyClass.class, MyImplClass.class,
 * constructorParams);
 * </pre>
 * This will allow to inject as follows:
 * <pre>
 * {@literal @}Inject
 * MyClass&lt;MyAggregate1, MyKey1&gt; mySuperClass; // inject instance of MyImplClass&lt;MyAggregate1, MyKey1&gt;
 * </pre>
 */
public class GenericBindingStrategy<T> implements BindingStrategy {
    /**
     * This class is the generic Guice assisted factory.
     */
    private static final Class<?> DEFAULT_IMPL_FACTORY_CLASS = GenericGuiceFactory.class;
    private final Class<T> injecteeClass;
    private final Class<? extends T> genericImplClass;
    private Map<Type[], Key<?>> constructorParamsMap;
    private Collection<Type[]> constructorParams;

    /**
     * Constructors.
     *
     * @param injecteeClass     the class to bind
     * @param genericImplClass  the implementation to bind with unresolved constructorParams
     * @param constructorParams the collection of resolved constructorParams
     */
    public GenericBindingStrategy(Class<T> injecteeClass, Class<? extends T> genericImplClass,
            Map<Type[], Key<?>> constructorParams) {
        this.constructorParamsMap = constructorParams;
        this.injecteeClass = injecteeClass;
        this.genericImplClass = genericImplClass;
    }

    /**
     * Constructors.
     *
     * @param injecteeClass     the class to bind
     * @param genericImplClass  the implementation to bind with unresolved constructorParams
     * @param constructorParams the collection of resolved constructorParams
     */
    public GenericBindingStrategy(Class<T> injecteeClass, Class<? extends T> genericImplClass,
            Collection<Type[]> constructorParams) {
        this.constructorParams = constructorParams;
        this.injecteeClass = injecteeClass;
        this.genericImplClass = genericImplClass;
    }

    @Override
    public void resolve(Binder binder) {
        // Bind all the possible types for one class or interface.
        // For instance: Repository<Customer,String>, Repository<Order, Long>, etc.
        FactoryModuleBuilder guiceFactoryBuilder = new FactoryModuleBuilder();

        if (constructorParamsMap != null) {
            for (Map.Entry<Type[], Key<?>> entry : constructorParamsMap.entrySet()) {
                bindKey(binder, guiceFactoryBuilder, entry.getKey(), entry.getValue());
            }
        } else {
            for (Type[] params : constructorParams) {
                bindKey(binder, guiceFactoryBuilder, params, null);
            }
        }

        TypeLiteral<?> guiceAssistedFactory = TypeLiteral.get(
                Types.newParameterizedType(DEFAULT_IMPL_FACTORY_CLASS, genericImplClass));
        binder.install(guiceFactoryBuilder.build(guiceAssistedFactory));
    }

    @SuppressWarnings("unchecked")
    private void bindKey(Binder binder, FactoryModuleBuilder guiceFactoryBuilder, Type[] params, Key<?> defaultKey) {
        // If a default key is provided use a linked binding to bind it
        if (defaultKey != null) {
            binder.bind(defaultKey.getTypeLiteral()).to((Key) defaultKey);
        }
        // Get the key to bind
        Key<T> key = BindingUtils.resolveKey(injecteeClass, genericImplClass, params);

        // Prepare the Guice provider
        Provider<?> provider = new GenericGuiceProvider<T>(genericImplClass, params);
        binder.requestInjection(provider);
        binder.bind(key).toProvider((Provider) provider);

        // Prepare the factory for assisted injection
        guiceFactoryBuilder.implement(key, (Class) genericImplClass);
    }
}
