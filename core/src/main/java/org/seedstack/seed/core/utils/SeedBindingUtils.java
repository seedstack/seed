/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

import com.google.common.reflect.TypeToken;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import javassist.Modifier;
import org.seedstack.seed.SeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class with various utility methods for java types.
 *
 * @author redouane.loulou@ext.mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 */
public final class SeedBindingUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeedBindingUtils.class);

    private SeedBindingUtils() {
        super();
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
    public static Map<Key<?>, Class<?>> resolveBindingDefinitions(Class<?> injecteeClass, Class<?> firstImplClass, Class<?>... restImplClasses) {
        Map<Key<?>, Class<?>> typeLiterals = new HashMap<>();
        List<Class<?>> subClasses = new ArrayList<>();

        if (firstImplClass != null) {
            subClasses.add(firstImplClass);
        }

        if (restImplClasses != null && restImplClasses.length > 0) {
            subClasses.addAll(Arrays.asList(restImplClasses));
        }

        for (Class<?> subClass : subClasses) {
            if (isNotBindable(subClass)) {
                LOGGER.trace("The class {} can't be bound", subClass);
            } else {
                Type resolvedType = TypeToken.of(subClass).getSupertype((Class) injecteeClass).getType();
                TypeLiteral<?> parentTypeLiteral;
                if (resolvedType == null) {
                    parentTypeLiteral = TypeLiteral.get(injecteeClass);
                } else {
                    parentTypeLiteral = TypeLiteral.get(resolvedType);
                }
                Annotation annotation = SeedReflectionUtils.getAnnotationMetaAnnotatedFromAncestor(subClass, Qualifier.class);
                Key<?> key;
                if (annotation != null) {
                    key = Key.get(parentTypeLiteral, annotation);
                } else {
                    key = Key.get(parentTypeLiteral);
                }
                if (typeLiterals.containsKey(key)) {
                    throw SeedException.createNew(CoreUtilsErrorCode.DUPLICATED_KEYS_FOUND).put("duplicatedKey", key)
                            .put("firstClass", subClass.getName()).put("secondClass", typeLiterals.get(key).getName());
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
    public static Map<Key<?>, Class<?>> resolveBindingDefinitions(Class<?> injecteeClass, Collection<Class<?>> implClasses) {
        if (implClasses != null && !implClasses.isEmpty()) {
            return resolveBindingDefinitions(injecteeClass, null, implClasses.toArray(new Class<?>[implClasses.size()]));
        }
        return new HashMap<>();
    }


    /**
     * Checks if the class is not an interface, an abstract class or a class with unresolved generics.
     *
     * @param subClass the class to check
     * @return true if the class verify the condition, false otherwise
     */
    private static boolean isNotBindable(Class<?> subClass) {
        return subClass.isInterface() || Modifier.isAbstract(subClass.getModifiers()) || subClass.getTypeParameters().length > 0;
    }

    /**
     * Resolve the key for injectee class, including qualifier from implClass and resolved type variables.
     * <p>
     * Useful when we can not resolve type variable from one implementation.
     *
     * @param injecteeClass       the injectee class
     * @param genericImplClass    the generic implementation
     * @param typeVariableClasses the type variable classes
     * @return {@link Key}
     */
    public static Key<?> resolveKey(Class<?> injecteeClass, Class<?> genericImplClass, Type... typeVariableClasses) {
        Annotation qualifier = SeedReflectionUtils.getAnnotationMetaAnnotated(genericImplClass, Qualifier.class);
        Key<?> key = null;
        TypeLiteral<?> genericInterface = TypeLiteral.get(Types.newParameterizedType(injecteeClass, typeVariableClasses));
        if (qualifier != null) {
            key = Key.get(genericInterface, qualifier);
        } else {
            key = Key.get(genericInterface);
        }
        return key;
    }
}
