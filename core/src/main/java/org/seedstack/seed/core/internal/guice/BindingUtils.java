/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.guice;

import com.google.common.reflect.TypeToken;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Qualifier;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.seedstack.shed.reflect.AnnotationPredicates;
import org.seedstack.shed.reflect.Annotations;

/**
 * Utilities for Guice bindings.
 */
public final class BindingUtils {
    private BindingUtils() {
        // no instantiation allowed
    }

    /**
     * Construct a checked map of bindable key with the corresponding implementation class.
     * <p>
     * Given the following injectee class and implementation classes :
     * <pre>
     * interface Sort&lt;T&gt; {...}
     *
     * {@literal @}Named("quick")
     * class StringQuickSort implements Sort&lt;String&gt; {...}
     *
     * {@literal @}Named("bubble")
     * class BubbleSort implements Sort&lt;String&gt; {...}
     *
     * class LongQuickSort implements Sort&lt;Long&gt; {...}
     * </pre>
     * The method will return a map with:
     * <pre>
     * Key[type=Sort&lt;String&gt;, annotation = Named[value = "quick"]] -&gt; StringQuickSort
     * Key[type=Sort&lt;String&gt;, annotation = Named[value = "bubble"]] -&gt; BubbleSort
     * Key[type=Sort&lt;Long&gt;] -&gt; LongQuickSort
     * </pre>
     * Possible injection patterns will be:
     * <pre>
     * {@literal @}Inject {@literal @}Name("quick")
     * Sort@lt;String&gt; stringQuickSort;
     *
     * {@literal @}Inject {@literal @}Name("bubble")
     * Sort@lt;String&gt; bubbleSort;
     *
     * {@literal @}Inject
     * Sort@lt;Long&gt; longQuickSort;
     * </pre>
     *
     * @param injecteeClass   the parent class to reach
     * @param firstImplClass  the first class
     * @param restImplClasses the sub classes
     * @return a multimap with typeliterals for keys and a list of associated subclasses for values
     * @throws SeedException when duplicates keys are found.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Map<Key<T>, Class<? extends T>> resolveBindingDefinitions(Class<T> injecteeClass,
            Class<? extends T> firstImplClass, Class<? extends T>... restImplClasses) {
        Map<Key<T>, Class<? extends T>> typeLiterals = new HashMap<>();
        List<Class<? extends T>> subClasses = new ArrayList<>();

        if (firstImplClass != null) {
            subClasses.add(firstImplClass);
        }
        if (restImplClasses != null && restImplClasses.length > 0) {
            subClasses.addAll(Arrays.asList(restImplClasses));
        }

        for (Class<? extends T> subClass : subClasses) {
            if (isBindable(subClass)) {
                Type resolvedType = TypeToken.of(subClass).getSupertype((Class) injecteeClass).getType();
                TypeLiteral<T> parentTypeLiteral;
                if (resolvedType == null) {
                    parentTypeLiteral = TypeLiteral.get(injecteeClass);
                } else {
                    parentTypeLiteral = (TypeLiteral<T>) TypeLiteral.get(resolvedType);
                }

                Optional<Annotation> qualifier = Annotations.on(subClass)
                        .traversingSuperclasses()
                        .traversingInterfaces()
                        .findAll()
                        .filter(AnnotationPredicates.annotationAnnotatedWith(Qualifier.class, false))
                        .findFirst();
                Key<T> key = qualifier.map(annotation -> Key.get(parentTypeLiteral, annotation)).orElseGet(
                        () -> Key.get(parentTypeLiteral));
                if (typeLiterals.containsKey(key)) {
                    throw SeedException.createNew(CoreErrorCode.DUPLICATED_BINDING_KEY)
                            .put("duplicatedKey", key)
                            .put("firstClass", subClass.getName())
                            .put("secondClass", typeLiterals.get(key).getName());
                }
                typeLiterals.put(key, subClass);
            }
        }

        return typeLiterals;
    }

    /**
     * Same as {@link #resolveBindingDefinitions(Class, Class, Class...)}.
     *
     * @param injecteeClass the parent class to reach
     * @param implClasses   the sub classes collection
     * @return a multimap with typeliterals for keys and a list of associated subclasses for values
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<Key<T>, Class<? extends T>> resolveBindingDefinitions(Class<T> injecteeClass,
            Collection<Class<? extends T>> implClasses) {
        if (implClasses != null && !implClasses.isEmpty()) {
            return resolveBindingDefinitions(injecteeClass, null, implClasses.toArray(new Class[implClasses.size()]));
        }
        return new HashMap<>();
    }

    /**
     * Checks if the class is not an interface, an abstract class or a class with unresolved generics.
     *
     * @param subClass the class to check
     * @return true if the class verify the condition, false otherwise
     */
    public static boolean isBindable(Class<?> subClass) {
        return !subClass.isInterface() && !Modifier.isAbstract(
                subClass.getModifiers()) && subClass.getTypeParameters().length == 0;
    }

    /**
     * Resolve the key for injectee class, including qualifier from implClass and resolved type variables.
     * <p>
     * Useful when we can not resolve type variable from one implementation.
     *
     * @param injecteeClass       the injectee class
     * @param genericImplClass    the generic implementation
     * @param typeVariableClasses the type variable classes
     * @return {@link com.google.inject.Key}
     */
    @SuppressWarnings("unchecked")
    public static <T> Key<T> resolveKey(Class<T> injecteeClass, Class<? extends T> genericImplClass,
            Type... typeVariableClasses) {
        Optional<Annotation> qualifier = Annotations.on(genericImplClass)
                .findAll()
                .filter(AnnotationPredicates.annotationAnnotatedWith(Qualifier.class, false))
                .findFirst();
        TypeLiteral<T> genericInterface = (TypeLiteral<T>) TypeLiteral.get(
                Types.newParameterizedType(injecteeClass, typeVariableClasses));
        return qualifier.map(annotation -> Key.get(genericInterface, annotation)).orElseGet(
                () -> Key.get(genericInterface));
    }

    /**
     * Tests if the class is a proxy.
     *
     * @param proxyClass The class to test.
     * @return true if class is proxy false otherwise.
     */
    public static boolean isProxy(Class<?> proxyClass) {
        return ProxyUtils.isProxy(proxyClass);
    }

    /**
     * Return the non proxy class if needed.
     *
     * @param toClean The class to clean.
     * @return the cleaned class.
     */
    public static Class<?> cleanProxy(Class<?> toClean) {
        return ProxyUtils.cleanProxy(toClean);
    }
}
