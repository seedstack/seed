/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.jsonhome;

import org.seedstack.seed.rest.api.Rel;
import org.seedstack.seed.rest.internal.JsonHomeSpecification;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to parse JAX-RS resource and return JSON-HOME resources.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
class ResourceParser {

    private final String baseRel;
    private final String baseParam;

    /**
     * Resource parser constructor.
     *
     * @param baseRel   the base URI for relation types
     * @param baseParam the base URI for parameters
     */
    public ResourceParser(String baseRel, String baseParam) {
        this.baseRel = baseRel;
        this.baseParam = baseParam;
    }

    /**
     * Parses a collection of JAX-RS resource classes. It returns a map of {@link org.seedstack.seed.rest.internal.jsonhome.Resource}.
     * If multiple class contains methods for the same resource, they will be merged.
     *
     * @param classes the resource classes
     * @return the map of JSON-HOME resources
     */
    public Map<String, Resource> parse(Collection<Class<?>> classes) {
        Map<String, Resource> resources = new HashMap<String, Resource>();
        for (Class<?> aClass : classes) {
            // parse the resource class
            Map<String, Resource> resourceMap = parse(aClass);

            // Merge with possible existing resource
            for (Map.Entry<String, Resource> entry : resourceMap.entrySet()) {
                mergeResources(resources, entry.getKey(), entry.getValue());
            }
        }
        return resources;
    }

    /**
     * Parses a JAX-RS resource class and returns a map of {@link org.seedstack.seed.rest.internal.jsonhome.Resource}.
     *
     * @param aClass the resource class
     * @return the map of JSON-HOME resources
     */
    public Map<String, Resource> parse(Class<?> aClass) {
        Map<String, Resource> resources = new HashMap<String, Resource>();
        for (Method method : aClass.getDeclaredMethods()) {
            Resource r = parse(method);
            if (r != null) {
                mergeResources(resources, r.rel(), r);
            }
        }
        return resources;
    }

    /**
     * Merge resources in the case where multiple methods expose the same resource
     * with different HTTP methods or media type for instance.
     *
     * @param registry      the map of resources
     * @param rel           the relation type to merge
     * @param otherResource the resource to merge
     */
    private void mergeResources(Map<String, Resource> registry, String rel, Resource otherResource) {
        Resource resource = registry.get(rel);
        if (resource != null) {
            resource.merge(otherResource);
            registry.put(rel, resource);
        } else {
            registry.put(rel, otherResource);
        }
    }

    /**
     * Parses a JAX-RS method and returns a JSON-HOME resource.
     *
     * @param method the method
     * @return the {@link org.seedstack.seed.rest.internal.jsonhome.Resource}
     */
    Resource parse(Method method) {
        String normalizedRel = findRel(method);
        if (normalizedRel == null) {
            return null;
        }

        String normalizedPath = findPath(method);
        Hints hints = new HintGenerator().findHint(method);

        if (UriBuilder.jaxTemplate(normalizedPath)) {
            Map<String, String> hrefVars = findHrefVars(method);
            return new Resource(normalizedRel, UriBuilder.stripJaxRsRegex(normalizedPath), hrefVars, hints);
        } else {
            return new Resource(normalizedRel, normalizedPath, hints);
        }
    }

    String findRel(Method method) {
        // Checks if @Rel is present with expose=true on the method then the class
        if (!new JsonHomeSpecification().isSatisfiedBy(method)) {
            return null;
        }

        // Compute the final rel
        Rel rel = method.getAnnotation(Rel.class);
        Rel rootRel = method.getDeclaringClass().getAnnotation(Rel.class);
        String classRel = null;
        if (rootRel != null) {
            classRel = rootRel.value();
        }
        String normalizedRel;
        if (classRel == null && rel == null) {
            // If no rel the resource won't be exposed
            normalizedRel = null;
        } else if (rel == null) {
            // If there is no annotation on the method we take the class rel
            normalizedRel = UriBuilder.path(baseRel, classRel);
        } else {
            // Otherwise always take the method annotation
            normalizedRel = UriBuilder.path(baseRel, rel.value());
        }
        return normalizedRel;
    }

    private String findPath(Method method) {
        Path path = method.getAnnotation(Path.class);
        Path rootPath = method.getDeclaringClass().getAnnotation(Path.class);
        String normalizedPath;
        if (rootPath != null && path != null) {
            normalizedPath = UriBuilder.path(rootPath.value(), path.value());
        } else if (path != null) {
            normalizedPath = UriBuilder.path(path.value());
        } else if (rootPath != null) {
            normalizedPath = UriBuilder.path(rootPath.value());
        } else {
            throw new IllegalArgumentException("Method " + method.getName() + " doesn't have a JAX-RS @Path specified");
        }
        return normalizedPath;
    }

    private Map<String, String> findHrefVars(Method method) {
        Map<String, String> hrefVars = new HashMap<String, String>();
        for (Annotation[] annotations : method.getParameterAnnotations()) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(PathParam.class)) {
                    String varName = ((PathParam) annotation).value();
                    hrefVars.put(varName, UriBuilder.path(baseParam, varName));
                } else if (annotation.annotationType().equals(QueryParam.class)) {
                    String varName = ((QueryParam) annotation).value();
                    hrefVars.put(varName, UriBuilder.path(baseParam, varName));
                }
            }
        }
        return hrefVars;
    }
}
