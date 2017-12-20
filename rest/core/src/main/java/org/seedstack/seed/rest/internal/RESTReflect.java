/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal;

import static org.seedstack.seed.rest.internal.UriBuilder.uri;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import org.seedstack.seed.rest.Rel;
import org.seedstack.shed.reflect.Classes;

/**
 * Provides a set of functions which scan HTTP methods to find various information
 * like rel, path, query parameters, etc.
 */
class RESTReflect {

    private RESTReflect() {
    }

    /**
     * Finds a the rel of a method. The rel can be found on the declaring class,
     * but the rel on the method will have the precedence.
     *
     * @param method the method to scan
     * @return the rel annotation
     */
    static Rel findRel(Method method) {
        Rel rootRel = method.getDeclaringClass().getAnnotation(Rel.class);
        Rel rel = method.getAnnotation(Rel.class);
        if (rel != null) {
            return rel;
        } else if (rootRel != null) {
            return rootRel;
        }
        return null;
    }

    /**
     * Finds the path of a method. If the declaring class and the method are both
     * annotated with {@literal @}Path, the paths will be concatenated.
     * <p>
     * Leading or ending slashes in paths are not taken in account. The method
     * will always add a leading slash and strip the ending slash.
     * </p>
     * <p>
     * If JAX-RS regex are present in the path, they will be replaced by
     * UriTemplate expressions.
     * </p>
     * For example:
     * <pre>
     * {@literal @}Path("widgets/{widgetName: [a-zA-Z][a-zA-Z_0-9]}/") --> /widgets/{widgetName}
     * </pre>
     *
     * @param method the method to scan
     * @return the resource's path
     */
    static String findPath(Method method) {
        Path pathFromClass = method.getDeclaringClass().getAnnotation(Path.class);
        Path pathFromMethod = method.getAnnotation(Path.class);

        String path = concatenatePathValues(pathFromClass, pathFromMethod);
        if (path == null) {
            return null;
        } else {
            path = addLeadingSlash(path);
            return UriBuilder.stripJaxRsRegex(path);
        }
    }

    private static String concatenatePathValues(Path pathFromClass, Path pathFromMethod) {
        boolean hasPathOnClass = pathFromClass != null;
        boolean hasPathOnMethod = pathFromMethod != null;
        String path = null;
        if (hasPathOnMethod && hasPathOnClass) {
            path = uri(pathFromClass.value(), pathFromMethod.value());
        } else if (hasPathOnClass) {
            path = uri(pathFromClass.value());
        } else if (hasPathOnMethod) {
            path = uri(pathFromMethod.value());
        }
        return path;
    }

    private static String addLeadingSlash(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    /**
     * Finds path parameters based on {@link javax.ws.rs.PathParam} annotation.
     *
     * @param baseParam the base parameter URI
     * @param method    the method to scan
     * @return the map of parameter URI by parameter name
     */
    static Map<String, String> findPathParams(String baseParam, Method method) {
        Map<String, String> hrefVars = new HashMap<>();
        for (Annotation[] paramAnnotations : method.getParameterAnnotations()) {
            for (Annotation paramAnnotation : paramAnnotations) {
                if (paramAnnotation.annotationType().equals(PathParam.class)) {
                    String varName = ((PathParam) paramAnnotation).value();
                    addHrefVar(baseParam, hrefVars, varName);
                }
            }
        }
        return hrefVars;
    }

    /**
     * Finds query parameters based on {@link javax.ws.rs.QueryParam} annotation.
     *
     * @param baseParam the base parameter URI
     * @param method    the method to scan
     * @return the map of parameter URI by parameter name
     */
    static Map<String, String> findQueryParams(String baseParam, Method method) {
        Map<String, String> hrefVars = new HashMap<>();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] paramAnnotations = parameterAnnotations[i];
            for (Annotation paramAnnotation : paramAnnotations) {
                Class<?> parameterClass = method.getParameterTypes()[i];
                hrefVars.putAll(findQueryParamOnParameter(baseParam, parameterClass, paramAnnotation));
            }
        }
        return hrefVars;
    }

    private static Map<String, String> findQueryParamOnParameter(String baseParam, Class<?> parameterClass,
            Annotation paramAnnotation) {
        Map<String, String> hrefVars = new HashMap<>();
        if (paramAnnotation.annotationType().equals(QueryParam.class)) {
            String varName = ((QueryParam) paramAnnotation).value();
            addHrefVar(baseParam, hrefVars, varName);
        } else if (Classes.optional("javax.ws.rs.BeanParam").isPresent() && paramAnnotation.annotationType().equals(
                BeanParam.class)) {
            hrefVars.putAll(findQueryParamOnFields(baseParam, parameterClass));
        }
        return hrefVars;
    }

    private static String addHrefVar(String baseParam, Map<String, String> hrefVars, String varName) {
        return hrefVars.put(varName, uri(baseParam, varName));
    }

    private static Map<String, String> findQueryParamOnFields(String baseParam, Class<?> aClass) {
        Map<String, String> hrefVars = new HashMap<>();
        for (Field field : aClass.getDeclaredFields()) {
            if (field.getAnnotation(QueryParam.class) != null) {
                addHrefVar(baseParam, hrefVars, field.getName());
            }
        }
        return hrefVars;
    }
}
