/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.reflections.ReflectionUtils;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.seedstack.seed.SeedException;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Class with various utility methods for reflection.
 *
 * @author epo.jemba@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 */
public final class SeedReflectionUtils {

    public static final String JAVA_LANG = "java.lang";

    private SeedReflectionUtils() {
    }

    /**
     * This methods take as parameter an annotatedElement (Class, Field, Method,
     * Constructor ) and an annotation class. It returns an annotation instance
     * that meet the requirement : instance of the class annotationClassToFind.
     * <p>
     * If annotatedElement is a class the method will reach all super type and
     * for each of those type, it will reach all implemented interfaces and for
     * each implemented interfaces it will reach all super interfaces
     * <p>
     * If annotatedElement is not a class the method will look for the
     * annotation class only in this element, other all the hierarchy.
     * <p>
     * From this list, the method will reach for the annotation class in all
     * annotation hierarchy.
     *
     * @param <T>                   the annotation type to retrieve
     * @param annotatedElement      The annotated element from where to start.
     * @param annotationClassToFind The annotation class to find.
     * @return ? extends Annotation
     */
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getMetaAnnotationFromAncestors(AnnotatedElement annotatedElement, Class<T> annotationClassToFind) {
        List<AnnotatedElement> list = Lists.newArrayList();

        if (annotatedElement instanceof Class<?>) {
            list.addAll(Arrays.asList(getAllInterfacesAndClasses((Class<?>) annotatedElement)));
        } else {
            list.add(annotatedElement);
        }

        for (AnnotatedElement element : list) {
            // element search
            for (Annotation anno : element.getAnnotations()) {
                if (anno.annotationType().equals(annotationClassToFind)) {
                    return (T) anno;
                }
            }

            // deep search
            for (Annotation anno : element.getAnnotations()) {
                T result = (T) getAnnotationDeep(anno, annotationClassToFind);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * This methods take as parameter an annotatedElement (Class, Field, Method,
     * Constructor ) and an annotation class. It returns an annotation instance
     * that meet the requirement : instance of the class annotationClassToFind.
     * <p>
     * If annotatedElement is a class the method will reach all super type and
     * for each of those type, it will reach all implemented interfaces and for
     * each implemented interfaces it will reach all super interfaces
     * <p>
     * If annotatedElement is not a class the method will look for the
     * annotation class in this element, and in the declaring class and all its ancestors.
     * <p>
     * From this list, the method will reach for the annotation class in all
     * annotation hierarchy.
     *
     * @param <T>                   the annotation type to retrieve
     * @param annotatedElement      The annotated element from where to start.
     * @param annotationClassToFind The annotation class to find.
     * @return ? extends Annotation
     */
    public static <T extends Annotation> T getMethodOrAncestorMetaAnnotatedWith(AnnotatedElement annotatedElement, Class<T> annotationClassToFind) {
        T result = null;
        if (Member.class.isAssignableFrom(annotatedElement.getClass())) {
            Class<?> declaringClass = Member.class.cast(annotatedElement).getDeclaringClass();
            result = SeedReflectionUtils.getMetaAnnotationFromAncestors(declaringClass, annotationClassToFind);
        }
        if (result == null) {
            return SeedReflectionUtils.getMetaAnnotationFromAncestors(annotatedElement, annotationClassToFind);
        } else {
            return result;
        }
    }

    /**
     * This method is looking for the annotation class toFind from the annoFrom
     * annotation instance. It will reach recursively until the annotation is
     * found.
     *
     * @param from   the annotation to search from.
     * @param toFind the annotation to find.
     * @return the found annotation or null if nothing found.
     */
    public static Annotation getAnnotationDeep(Annotation from, Class<? extends Annotation> toFind) {

        if (from.annotationType().equals(toFind)) {
            return from;
        } else {
            for (Annotation anno : from.annotationType().getAnnotations()) {
                if (!anno.annotationType().getPackage().getName().startsWith(JAVA_LANG)) {
                    return getAnnotationDeep(anno, toFind);
                }
            }
        }

        return null;
    }

    /**
     * Check if the element is annotated or meta-annotated with the annotationClass.
     *
     * @param annotatedElement The class to search from.
     * @param annotationClass  The annotation class to search.
     * @return true if annotation is present, false otherwise.
     */
    public static boolean hasAnnotationDeep(AnnotatedElement annotatedElement, Class<? extends Annotation> annotationClass) {

        if (annotatedElement.equals(annotationClass)) {
            return true;
        }

        for (Annotation anno : annotatedElement.getAnnotations()) {
            Class<? extends Annotation> annoClass = anno.annotationType();
            if (!annoClass.getPackage().getName().startsWith(JAVA_LANG) && hasAnnotationDeep(annoClass, annotationClass)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns all the interfaces and classes implemented or extended by a class.
     *
     * @param clazz The class to search from.
     * @return The array of classes and interfaces found.
     */
    public static Class<?>[] getAllInterfacesAndClasses(Class<?> clazz) {
        return getAllInterfacesAndClasses(new Class[]{clazz});
    }

    /**
     * This method walks up the inheritance hierarchy to make sure we get every
     * class/interface extended or implemented by classes.
     *
     * @param classes The classes array used as search starting point.
     * @return the found classes and interfaces.
     */
    @SuppressWarnings("unchecked")
    public static Class<?>[] getAllInterfacesAndClasses(Class<?>[] classes) {
        if (0 == classes.length) {
            return classes;
        } else {
            List<Class<?>> extendedClasses = new ArrayList<>();
            // all interfaces hierarchy
            for (Class<?> clazz : classes) {
                if (clazz != null) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    if (interfaces != null) {
                        extendedClasses.addAll(Arrays.asList(interfaces));
                    }
                    Class<?> superclass = clazz.getSuperclass();
                    if (superclass != null && superclass != Object.class) {
                        extendedClasses.addAll(Arrays.asList(superclass));
                    }
                }
            }

            // Class::getInterfaces() gets only interfaces/classes
            // implemented/extended directly by a given class.
            // We need to walk the whole way up the tree.
            return (Class[]) ArrayUtils.addAll(classes, getAllInterfacesAndClasses(extendedClasses.toArray(new Class[extendedClasses.size()])));
        }
    }

    /**
     * Tests if the class is a proxy.
     *
     * @param proxyClass The class to test.
     * @return true if class is proxy false otherwise.
     */
    public static boolean isProxy(Class<?> proxyClass) {
        return proxyClass.getName().contains("EnhancerByGuice");
    }

    /**
     * Return the non proxy class if needed.
     *
     * @param toClean The class to clean.
     * @return the cleaned class.
     */
    public static Class<?> cleanProxy(Class<?> toClean) {
        if (SeedReflectionUtils.isProxy(toClean)) {
            return toClean.getSuperclass();
        }

        return toClean;
    }

    /**
     * Find the most complete class loader by trying the current thread context class loader, then the classloader of the
     * given class if any, then the class loader that loaded SEED core, then the system class loader.
     *
     * @param target the class to get the class loader from if no current thread context class loader is present. May be null.
     * @return the most complete class loader it found.
     */
    public static ClassLoader findMostCompleteClassLoader(Class<?> target) {
        // Try the most complete class loader we can get
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // Then fallback to the class loader from a specific class given
        if (classLoader == null && target != null) {
            classLoader = target.getClassLoader();
        }

        // Then fallback to the class loader that loaded SEED core
        if (classLoader == null) {
            classLoader = SeedReflectionUtils.class.getClassLoader();
        }

        // Then fallback to the system class loader
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        // Throws exception if no classloader was found at all
        if (classLoader == null) {
            throw SeedException.createNew(CoreErrorCode.UNABLE_TO_FIND_CLASSLOADER);
        }

        return classLoader;
    }

    /**
     * Find the most complete class loader by trying the current thread context class loader, then the class loader
     * that loaded SEED core, then the system class loader.
     *
     * @return the most complete class loader it found.
     */
    public static ClassLoader findMostCompleteClassLoader() {
        return SeedReflectionUtils.findMostCompleteClassLoader(null);
    }

    /**
     * Returns if a class is present in the classpath without initializing it.
     *
     * @param name the class name to check.
     * @return true if the class is present in the classpath, false otherwise.
     */
    public static boolean isClassPresent(String name) {
        try {
            Class.forName(name, false, findMostCompleteClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Given the class targetClass (not an interface) the method will return all matched methods
     * for method. Methods will be reached in extended super classes or implemented interfaces.
     *
     * @param method from which the search starts.
     * @return a set of methods grabbed from parent classes and interfaces.
     */
    public static Set<Method> methodsFromAncestors(final Method method) {
        // Fetching annotations from method (we fetch all methods in super and
        // interfaces)

        Class<?> targetClass = method.getDeclaringClass();

        Predicate<? super Method> predicate = (Predicate<Method>) input -> !method.equals(input) && methodsAreEquivalent(method, input);

        Set<Method> methods = new HashSet<>();
        methods.add(method);
        methods.addAll(ReflectionUtils.getAllMethods(targetClass, predicate));

        return methods;
    }

    /**
     * This method iterates on method parameter and go through all method hierarchy to find the given annotation. If the
     * annotation if found on a parameter in the method hierarchy, the annotation is returned in the array at the
     * parameter index.
     *
     * @param method          the method to check
     * @param annotationClass the annotation to found
     * @return array of annotation found
     */
    public static Annotation[] parameterAnnotationsFromAncestors(final Method method, Class<? extends Annotation> annotationClass) {

        Set<Annotation[][]> allParametersAnnotationsFromAncestors = allParametersAnnotationsFromAncestors(method);

        Annotation[] annoArray = new Annotation[method.getParameterTypes().length];

        // all ancestors methods
        for (Annotation[][] parametersAnnotations : allParametersAnnotationsFromAncestors) {
            int i = 0;
            // all parameters of one methods
            for (Annotation[] parametersAnnotation : parametersAnnotations) {

                // iterate over annotation on 1 parameters
                for (Annotation anno : parametersAnnotation) {

                    if (anno.annotationType().equals(annotationClass)) {
                        annoArray[i] = anno;
                        break;
                    }
                }

                i++;
            }
        }

        return annoArray;

    }


    /**
     * Finds all parameter annotations of a method including its ancestors.
     *
     * @param method from which the search starts.
     * @return a set of methods grabed from parent classes and interfaces.
     */
    public static Set<Annotation[][]> allParametersAnnotationsFromAncestors(final Method method) {

        Set<Method> methodsFromAncestors = methodsFromAncestors(method);

        Set<Annotation[][]> parametersAnnotations = new HashSet<>();

        for (Method m : methodsFromAncestors) {
            Annotation[][] as = m.getParameterAnnotations();
            if (as != null) {
                parametersAnnotations.add(as);
            }
        }

        return parametersAnnotations;
    }

    /**
     * Checks if an annotation is present on a set of methods (itself or on at lest one of its parameters) set.
     *
     * @param methods         the methods where the annotation is searched.
     * @param annotationClass the annotation to check for.
     * @return true if the annotation is present, false otherwise.
     */
    public static boolean isPresent(Set<Method> methods, Class<? extends Annotation> annotationClass) {
        for (Method method : methods) {
            if (method.getAnnotation(annotationClass) != null) {
                return true;
            }

            for (Annotation[] paramAnnotations : method.getParameterAnnotations()) {
                if (paramAnnotations != null) {
                    for (Annotation paramAnnotation : paramAnnotations) {
                        if (paramAnnotation.annotationType().equals(annotationClass)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks if two methods are equivalent (same name, same parameter types, same return types).
     *
     * @param left  the first method to check.
     * @param right the second method to check.
     * @return true if methods are equivalent, false otherwise.
     */
    public static boolean methodsAreEquivalent(Method left, Method right) {
        EqualsBuilder builder = new EqualsBuilder()
                .append(left.getName(), right.getName())
                .append(left.getParameterTypes(), right.getParameterTypes())
                .append(left.getReturnType(), right.getReturnType());

        return builder.isEquals();
    }

    /**
     * Find the caller of a method.
     *
     * @param self the instance within the check is made.
     * @return the found StackTraceElement or null if not found.
     */
    public static StackTraceElement findCaller(Object self) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (!stackTraceElement.getClassName().equals(Thread.class.getCanonicalName()) &&
                    !stackTraceElement.getClassName().equals(SeedReflectionUtils.class.getCanonicalName()) &&
                    !stackTraceElement.getClassName().equals(self.getClass().getCanonicalName())
                    ) {
                return stackTraceElement;
            }
        }

        return null;
    }

    /**
     * Finds a annotation meta-annotated on the given annotated element.
     * <p>
     * If the annotated element is a class, the method will go through all its parent class and interface to find an
     * annotation meta annotated.
     *
     * @param <T>                   the annotation type to retrieve
     * @param annotatedElementClass the annotated element to reach
     * @param metaAnnotationToFind  the meta annotation to find
     * @return the annotation meta annotated if exist, null otherwise
     */
    public static <T extends Annotation> Annotation getAnnotationMetaAnnotatedFromAncestor(
            Class<?> annotatedElementClass, Class<T> metaAnnotationToFind) {
        Annotation[] annotations = annotatedElementClass.getAnnotations();
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                Annotation metaAnnotation = SeedReflectionUtils.getMetaAnnotationFromAncestors(
                        annotation.getClass(), metaAnnotationToFind);
                if (metaAnnotation != null) {
                    return annotation;
                }
            }
        }
        return null;
    }

    /**
     * Finds a annotation meta-annotated on the given annotated element.
     *
     * @param <T>                   the annotation type to retrieve
     * @param annotatedElementClass the annotated element
     * @param metaAnnotationToFind  the meta annotation to find
     * @return the annotation meta annotated if exist, null otherwise
     */
    public static <T extends Annotation> Annotation getAnnotationMetaAnnotated(
            AnnotatedElement annotatedElementClass, Class<T> metaAnnotationToFind) {
        Annotation[] annotations = annotatedElementClass.getAnnotations();
        for (Annotation annotation : annotations) {
            Annotation[] metaAnnotations = annotation.annotationType().getAnnotations();
            for (Annotation metaAnnotation : metaAnnotations) {
                if (metaAnnotation.annotationType().equals(metaAnnotationToFind)) {
                    return annotation;
                }
            }
        }
        return null;
    }

    /**
     * Invoke a named method through reflection on the target object.
     *
     * @param target the object to invoke the method on.
     * @param method the name of the method.
     * @param args   the arguments array to pass to the method.
     * @return the return value of the invoked method.
     */
    public static Object invokeMethod(Object target, String method, Object... args) {
        Class[] classes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            classes[i] = args[i].getClass();
        }

        try {
            Method m = target.getClass().getDeclaredMethod(method, classes);
            m.setAccessible(true);
            return m.invoke(target, args);
        } catch (Exception e) {
            throw SeedException.wrap(e, CoreUtilsErrorCode.METHOD_INVOCATION_FAILED);
        }
    }

    /**
     * Retrieve a field value through reflection on the target object.
     *
     * @param target    the object to get the field value from.
     * @param fieldName the field name.
     * @return the field value.
     */
    public static Object getFieldValue(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw SeedException.wrap(e, CoreUtilsErrorCode.GET_FIELD_VALUE_FAILED).put("field", fieldName).put("class", target.getClass().getName());
        }
    }

    /**
     * Set a field value through reflection on the target object.
     *
     * @param target    the object to set the field on.
     * @param fieldName the field name.
     * @param value     the field value.
     */
    public static void setFieldValue(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw SeedException.wrap(e, CoreUtilsErrorCode.SET_FIELD_VALUE_FAILED).put("field", fieldName).put("class", target.getClass().getName());
        }
    }

    /**
     * Checks if a class exists in the classpath.
     *
     * @param dependency class to look for.
     * @return an {@link Optional} of the class (empty if class is not present).
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<Class<T>> optionalOfClass(String dependency) {
        try {
            return Optional.of((Class<T>) Class.forName(dependency));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }
}
