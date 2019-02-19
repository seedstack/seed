/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration.tool.utils;

import org.seedstack.seed.Configuration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationUtils {

    // Find list of Configuration-annotated fields in myClass
    public static Map<String, List<Class>> extractConfigurationAnnotatedFields(Class<?> myClass) {
        Map<String, List<Class>> annotationValues = new HashMap<>();
        List<String> annotationsInClass = getConfigurationAnnotations(myClass);
        for (String annotationValue : annotationsInClass) {
            if (!annotationValues.containsKey(annotationValue)) {
                annotationValues.put(annotationValue, new ArrayList<>());
            }
            annotationValues.get(annotationValue).add(myClass);
        }

        return annotationValues;
    }

    /** Create a list of @Configuration annotations present in myClass.
     *
     * @param myClass
     * @return
     */
    public static List<String> getConfigurationAnnotations(Class myClass) {
        List<String> result = new ArrayList<>();

        Field[] fields = myClass.getDeclaredFields();
        for (Field field : fields) {
            Configuration annotation = field.getDeclaredAnnotation(Configuration.class);
            if (annotation != null) {
                for (String value : annotation.value()) {
                    result.add(value);
                }
            }
        }
        return result;
    }
}
