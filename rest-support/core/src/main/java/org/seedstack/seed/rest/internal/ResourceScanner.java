/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import com.damnhandy.uri.template.UriTemplate;
import com.damnhandy.uri.template.UriTemplateBuilder;
import org.seedstack.seed.rest.api.hal.Link;
import org.seedstack.seed.rest.internal.jsonhome.HintScanner;
import org.seedstack.seed.rest.internal.jsonhome.Hints;
import org.seedstack.seed.rest.internal.jsonhome.Resource;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Scans the JAX-RS resources for building JSON-HOME resources and HAL links.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
class ResourceScanner {

    private static final RelSpecification REL_SPECIFICATION = new RelSpecification();
    private static final JsonHomeSpecification JSON_HOME_SPECIFICATION = new JsonHomeSpecification();

    private final Map<String, List<Method>> resourceByRel = new HashMap<String, List<Method>>();
    private final Map<String, Resource> jsonHomeResources = new HashMap<String, Resource>();
    private final Map<String, Link> halLinks = new HashMap<String, Link>();

    private final String baseRel;
    private final String baseParam;

    /**
     * Constructor.
     *
     * @param baseRel   the base URI for the relation types
     * @param baseParam the base URI for the href parameters
     */
    public ResourceScanner(final String baseRel, final String baseParam) {
        this.baseRel = baseRel;
        this.baseParam = baseParam;
    }

    /**
     * Scans a collection of resources.
     *
     * @param classes the resource to scan
     * @return itself
     */
    public ResourceScanner scan(final Collection<Class<?>> classes) {
        // collect method to scan
        for (Class<?> aClass : classes) {
            scan(aClass);
        }
        // do the actual work
        buildJsonHomeResources();
        buildHalLink();
        return this;
    }

    private void scan(Class<?> aClass) {
        // Collects all the class's HTTP methods with a rel
        // (all of them if the rel is on the class)
        for (Method method : aClass.getDeclaredMethods()) {
            if (REL_SPECIFICATION.isSatisfiedBy(method)) {
                String rel = RESTReflect.findRel(method).value();
                if ("".equals(rel)) {
                    throw new IllegalStateException("Missing rel value on " + method.toGenericString());
                }
                List<Method> methods = resourceByRel.get(rel);
                if (methods == null) {
                    methods = new ArrayList<Method>();
                }
                methods.add(method);
                resourceByRel.put(rel, methods);
            }
        }
    }

    /**
     * Returns the JSON-HOME resources.
     *
     * @return resource map
     */
    public Map<String, Resource> jsonHomeResources() {
        return jsonHomeResources;
    }

    /**
     * Returns the HAL links.
     *
     * @return the link map
     */
    public Map<String, Link> halLinks() {
        return halLinks;
    }

    private void buildJsonHomeResources() {
        for (Map.Entry<String, List<Method>> entry : resourceByRel.entrySet()) {
            // Extends the rel with baseRel
            String rel = entry.getKey();
            String absoluteRel = UriBuilder.path(baseRel, rel);

            Resource resource = null;
            List<Method> methods = entry.getValue();
            for (Method method : methods) {
                Resource currentResource = buildJsonHomeResource(baseParam, absoluteRel, method);
                if (resource == null) {
                    resource = currentResource;
                } else {
                    resource.merge(currentResource);
                }
            }
            if (resource != null) {
                jsonHomeResources.put(absoluteRel, resource);
            }
        }
    }

    private Resource buildJsonHomeResource(String baseParam, String rel, Method method) {
        Resource currentResource = null;

        // Check if the resource should be exposed
        if (JSON_HOME_SPECIFICATION.isSatisfiedBy(method)) {

            String path = RESTReflect.findPath(method);
            if (path == null) {
                return null;
            }

            Hints hints = new HintScanner().findHint(method);

            if (isTemplated(path)) {
                Map<String, String> pathParams = RESTReflect.findPathParams(baseParam, method);
                Map<String, String> queryParams = RESTReflect.findQueryParams(baseParam, method);
                currentResource = new Resource(rel, path, pathParams, queryParams, hints);
            } else {
                currentResource = new Resource(rel, path, hints);
            }
        }
        return currentResource;
    }

    private boolean isTemplated(String path) {
        return UriTemplate.fromTemplate(path).expressionCount() > 0;
    }

    private void buildHalLink() {
        for (Map.Entry<String, List<Method>> entry : resourceByRel.entrySet()) {

            Map<String, String> queryParams = new HashMap<String, String>();
            for (Method method : entry.getValue()) {
                queryParams.putAll(RESTReflect.findQueryParams("", method));
            }

            String path = RESTReflect.findPath(entry.getValue().get(0));
            if (path == null) {
                throw new IllegalStateException("Path not found for rel: " + entry.getKey());
            }

            UriTemplateBuilder uriTemplateBuilder = UriTemplate.buildFromTemplate(path);
            if (!queryParams.isEmpty()) {
                uriTemplateBuilder.query(queryParams.keySet().toArray(new String[queryParams.keySet().size()]));
            }
            halLinks.put(entry.getKey(), new Link(uriTemplateBuilder.build().getTemplate()));
        }
    }
}
