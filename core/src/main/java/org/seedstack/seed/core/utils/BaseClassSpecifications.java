/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

import org.apache.commons.lang.ArrayUtils;
import org.kametic.specifications.AbstractSpecification;
import org.kametic.specifications.AndSpecification;
import org.kametic.specifications.NotSpecification;
import org.kametic.specifications.OrSpecification;
import org.kametic.specifications.Specification;
import org.kametic.specifications.reflect.ClassMethodsAnnotatedWith;
import org.kametic.specifications.reflect.DescendantOfSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class BaseClassSpecifications {

    private static final Logger logger = LoggerFactory.getLogger(BaseClassSpecifications.class);

    public static Specification<Class<?>> classIs(final Class<?> attendee) {
        return new AbstractSpecification<Class<?>>() {

            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {

                return candidate != null && candidate.equals(attendee);
            }

        };
    }

    public static Specification<Class<?>> classAnnotatedWith(final Class<? extends Annotation> klass) {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {
                return candidate != null && candidate.getAnnotation(klass) != null;
            }
        };
    }

    public static Specification<Class<?>> descendantOf(Class<?> ancestor) {
        return new DescendantOfSpecification(ancestor);
    }

    /**
     * @param modifier the expected modifier
     * @return a specification which checks the class modifier
     */
    public static Specification<Class<?>> classModifierIs(final int modifier) {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {
                return (candidate.getModifiers() & modifier) != 0;

            }
        };
    }

    public static Specification<Class<?>> classImplements(final Class<?> klass) {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {
                if (candidate != null && klass.isInterface()) {
                    for (Class<?> i : candidate.getInterfaces()) {
                        if (i.equals(klass)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    /**
     * @return a specification which check if a least one constructor is public
     */
    public static Specification<Class<?>> classConstructorIsPublic() {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {

                for (Constructor<?> constructor : candidate.getDeclaredConstructors()) {
                    if (Modifier.isPublic(constructor.getModifiers())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * @param classes the list of class to check
     * @return a specification which check if classes contain the candidate class
     */
    public static Specification<Class<?>> classIsIn(final Collection<Class<?>> classes) {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {
                return candidate != null && classes != null && classes.contains(candidate);
            }
        };
    }

    /**
     * @param interfaceClass the requested interface
     * @return a specification which check if one candidate ancestor implements the given interface
     */
    public static Specification<Class<?>> ancestorImplements(final Class<?> interfaceClass) {
        return new AbstractSpecification<Class<?>>() {

            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {
                if (candidate == null) {
                    return false;
                }

                boolean result = false;

                Class<?>[] allInterfacesAndClasses = getAllInterfacesAndClasses(candidate);

                for (Class<?> clazz : allInterfacesAndClasses) {
                    if (!clazz.isInterface()) {
                        for (Class<?> i : clazz.getInterfaces()) {
                            if (i.equals(interfaceClass)) {
                                result = true;
                                break;
                            }
                        }
                    }
                }

                return result;
            }

        };
    }

    /**
     * Checks if the candidate has one field annotated or meta annotated by the given annotation.
     *
     * @param annotationClass the requested annotation
     * @return the specification
     */
    public static Specification<Class<?>> fieldDeepAnnotatedWith(final Class<? extends Annotation> annotationClass) {

        return new AbstractSpecification<Class<?>>() {

            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {
                try {
                    if (candidate != null) {
                        do {
                            for (Field field : candidate.getDeclaredFields()) {
                                if (field.isAnnotationPresent(annotationClass)) {
                                    return true;
                                }
                            }
                            candidate = candidate.getSuperclass();
                        } while (candidate != null && candidate != Object.class);
                    }
                } catch (Throwable error) {
                    logger.trace(String.format("Warning in Specification fieldAnnotatedWith. Candidate: %s, annotation: %s", candidate.getSimpleName(), annotationClass.getSimpleName()), error);
                }

                return false;
            }
        };
    }

    /**
     * Checks if the candidate inherits from the given class.
     *
     * @param klass the requested class
     * @return the specification
     */
    public static Specification<Class<?>> classInherits(final Class<?> klass) {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {
                return candidate != null && klass.isAssignableFrom(candidate);
            }
        };
    }

    public static Specification<Class<?>> classMethodsAnnotatedWith(final Class<? extends Annotation> annotationClass) {
        return new ClassMethodsAnnotatedWith(annotationClass);
    }

    /**
     * Checks if the candidate or an ancestor is annotated or meta annotated by the given annotation.
     *
     * @param anoKlass the requested annotation
     * @return the specification
     */
    public static Specification<Class<?>> ancestorMetaAnnotatedWith(final Class<? extends Annotation> anoKlass) {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {

                if (candidate == null) {
                    return false;
                }

                boolean result = false;

                Class<?>[] allInterfacesAndClasses = getAllInterfacesAndClasses(candidate);

                for (Class<?> clazz : allInterfacesAndClasses) {
                    boolean satisfiedBy = classMetaAnnotatedWith(anoKlass).isSatisfiedBy(clazz);
                    if (satisfiedBy) {
                        result = true;
                        break;
                    }
                }

                return result;
            }
        };
    }

    /**
     * Checks if the candidate is annotated or meta annotated by the given annotation.
     *
     * @param klass the requested annotation
     * @return the specification
     */
    public static Specification<Class<?>> classMetaAnnotatedWith(final Class<? extends Annotation> klass) {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {
                return candidate != null && hasAnnotationDeep(candidate, klass);
            }
        };

    }

    /**
     * Checks if the given class is annotated or meta annotated with the given annotation.
     *
     * @param aClass          the class to check
     * @param annotationClass the requested annotation
     * @return true if the class if annotated or meta annotated, false otherwise
     */
    public static boolean hasAnnotationDeep(Class<?> aClass, Class<? extends Annotation> annotationClass) {
        if (aClass.equals(annotationClass)) {
            return true;
        }

        for (Annotation anno : aClass.getAnnotations()) {
            Class<? extends Annotation> annoClass = anno.annotationType();
            if (!annoClass.getPackage().getName().startsWith("java.lang")
                    && hasAnnotationDeep(annoClass, annotationClass)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the candidate is an interface.
     *
     * @return the specification
     */
    public static Specification<Class<?>> classIsInterface() {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {

                return candidate != null && candidate.isInterface();
            }
        };
    }

    /**
     * Checks if the candidate equals to the given class.
     *
     * @param notCandidate the class to check
     * @return the specification
     */
    public static Specification<Class<?>> classIsNot(final Class<?> notCandidate) {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {

                return candidate != null && !candidate.equals(notCandidate);
            }
        };
    }

    /**
     * Checks if the candidate has interface.
     *
     * @return the specification
     */
    public static Specification<Class<?>> classHasSuperInterfaces() {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {
                Class<?>[] interfaces = {};
                if (candidate != null) {
                    interfaces = candidate.getInterfaces();
                }
                return candidate != null && interfaces != null && interfaces.length > 0;
            }
        };
    }

    /**
     * Checks if the class is an annotation
     *
     * @return the specification
     */
    public static Specification<Class<?>> classIsAnnotation() {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {

                return candidate != null && candidate.isAnnotation();
            }
        };
    }

    /**
     * Checks if the class is abstract.
     *
     * @return the specification
     */
    public static Specification<Class<?>> classIsAbstract() {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {

                return candidate != null && Modifier.isAbstract(candidate.getModifiers());
            }
        };
    }

    /**
     * Checks if at least one method of the class is annotated with the annotation class.
     *
     * @param annotationClass the requested annotation
     * @return the specification
     */
    public static Specification<Class<?>> methodAnnotatedWith(final Class<? extends Annotation> annotationClass) {
        return new ClassMethodsAnnotatedWith(annotationClass);
    }

    static Class<?>[] getAllInterfacesAndClasses(Class<?> clazz) {
        return getAllInterfacesAndClasses(new Class[]{clazz});
    }

    /**
     * This method walks up the inheritance hierarchy to make sure we get every class/interface/superclass/interface's superclass that could
     * possibly contain the declaration of the annotated method we're looking for.
     *
     * @param classes array of class
     * @return array of class
     */
    @SuppressWarnings("unchecked")
    static Class<?>[] getAllInterfacesAndClasses(Class<?>[] classes) {
        if (0 == classes.length) {
            return classes;
        } else {
            List<Class<?>> extendedClasses = new ArrayList<>();
            // all interfaces hierarchy
            for (Class<?> clazz : classes) {
                if (clazz != null) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    if (interfaces != null) {
                        extendedClasses
                                .addAll(Arrays
                                        .asList(interfaces));
                    }
                    Class<?> superclass = clazz.getSuperclass();
                    if (superclass != null && superclass != Object.class) {
                        extendedClasses
                                .addAll(Arrays
                                        .asList(superclass));
                    }
                }
            }

            // Class::getInterfaces() gets only interfaces/classes
            // implemented/extended directly by a given class.
            // We need to walk the whole way up the tree.
            return (Class[]) ArrayUtils.addAll(classes,
                    getAllInterfacesAndClasses(extendedClasses
                            .toArray(new Class[extendedClasses.size()])));
        }
    }

    /**
     * Logical OR between the specifications.
     *
     * @param participants array of specification
     * @return the specification
     */
    public static Specification<Class<?>> or(Specification<Class<?>>... participants) {
        return new OrSpecification<>(participants);
    }

    /**
     * Logical AND between the specifications.
     *
     * @param participants array of specification
     * @return the specification
     */
    public static Specification<Class<?>> and(Specification<Class<?>>... participants) {
        return new AndSpecification<>(participants);
    }

    /**
     * The negation of the given specification
     *
     * @param participant the specification
     * @return the specification
     */
    public static Specification<Class<?>> not(Specification<Class<?>> participant) {
        return new NotSpecification<>(participant);
    }
}
