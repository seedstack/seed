/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import org.seedstack.seed.rest.api.Rel;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a set of functions which scan HTTP methods to find various information
 * like rel, path, query parameters, etc.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
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
        Path rootPath = method.getDeclaringClass().getAnnotation(Path.class);
        Path path = method.getAnnotation(Path.class);

        // Concatenates paths form class and method
        String pathValue = null;
        if (path != null && rootPath != null) {
            pathValue = UriBuilder.uri(rootPath.value(), path.value());
        } else if (rootPath != null) {
            pathValue = UriBuilder.uri(rootPath.value());
        } else if (path != null) {
            pathValue = UriBuilder.uri(path.value());
        }

        if (pathValue != null) {
            // Path should always starts with a slash
            if (!pathValue.startsWith("/")) {
                pathValue = "/" + pathValue;
            }

            // Extract JAX-RS regex from the path
            return UriBuilder.stripJaxRsRegex(pathValue);
        } else {
            return null;
        }
    }

    /**
     * Finds path parameters based on {@link javax.ws.rs.PathParam} annotation.
     *
     * @param baseParam the base parameter URI
     * @param method    the method to scan
     * @return the map of parameter URI by parameter name
     */
    static Map<String, String> findPathParams(String baseParam, Method method) {
        Map<String, String> hrefVars = new HashMap<String, String>();
        for (Annotation[] annotations : method.getParameterAnnotations()) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(PathParam.class)) {
                    String varName = ((PathParam) annotation).value();
                    hrefVars.put(varName, UriBuilder.uri(baseParam, varName));
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
        Map<String, String> hrefVars = new HashMap<String, String>();
        for (Annotation[] annotations : method.getParameterAnnotations()) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(QueryParam.class)) {
                    String varName = ((QueryParam) annotation).value();
                    hrefVars.put(varName, UriBuilder.uri(baseParam, varName));
                }
            }
        }
        return hrefVars;
    }
}
