/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration.tool.utils;

import org.seedstack.seed.Configuration;
import org.seedstack.shed.reflect.Classes;
import org.seedstack.shed.reflect.Types;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClassUtils {
    // retrieve field 'name' from configClass, including parent classes and interfaces
    public static Optional<Field> getField(Class<?> configClass, String name) {
        if (configClass == null) {
            return Optional.empty();
        } else {
            return Classes.from(configClass).traversingSuperclasses().traversingInterfaces().field(name);
        }
    }

    /**
     * Return Field from configClass annotated with {link org.seedstack.seed.Configuration) and named with name
     * @param configClass class to search into
     * @param name name of the field to be looked for
     * @param annotation annotation type (@Configuration here)
     * @return
     */
    public static Optional<Field> getField(Class<?> configClass, String name, Class<Configuration> annotation) {
        if (configClass == null) {
            return Optional.empty();
        } else {
            List<Field> fields = Classes.from(configClass).traversingSuperclasses().traversingInterfaces().fields().collect(Collectors.toList());
            for (Field field : fields) {
                if (field.isAnnotationPresent(annotation)) {
                    if (field.getAnnotation(annotation).value().length > 0 && field.getAnnotation(annotation).value()[0].equals(name)) {
                        return Optional.of(field);
                    }
                }
            }
            return Optional.empty();
        }
    }

    public static Class getDeclaringClass(Field field) {
        Type genericType = field.getGenericType();
        Class<?> rawClass = Types.rawClassOf(genericType);
        Type itemType = null;
        if (Collection.class.isAssignableFrom(rawClass) && genericType instanceof ParameterizedType) {
            itemType = Types.rawClassOf(((ParameterizedType) genericType).getActualTypeArguments()[0]);
        } else if (Map.class.isAssignableFrom(rawClass) && genericType instanceof ParameterizedType) {
            itemType = Types.rawClassOf(((ParameterizedType) genericType).getActualTypeArguments()[1]);
        } else if (genericType instanceof Class<?> && ((Class<?>) genericType).isArray()) {
            itemType = ((Class<?>) genericType).getComponentType();
        }
        // not a ParameterizedType; probably a primitive
        if (itemType == null) {
            return rawClass;
        } else {
            return Types.rawClassOf(itemType);
        }
    }

    // We consider arrays as primitive classes in this context, whatever they are made of.
    // Same for arrays, and lists
    public static boolean isPrimitive(Class someClass) {
        if (boolean.class.equals(someClass) || Boolean.class.equals(someClass)
                || int.class.equals(someClass) || Integer.class.equals(someClass)
                || long.class.equals(someClass) || Long.class.equals(someClass)
                || short.class.equals(someClass) || Short.class.equals(someClass)
                || float.class.equals(someClass) || Float.class.equals(someClass)
                || double.class.equals(someClass) || Double.class.equals(someClass)
                || byte.class.equals(someClass) || Byte.class.equals(someClass)
                || char.class.equals(someClass) || Character.class.equals(someClass)
                || String.class.equals(someClass)) {
            return true;
        }
        return false;
    }
}