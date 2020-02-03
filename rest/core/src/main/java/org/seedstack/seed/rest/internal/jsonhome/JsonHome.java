/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.jsonhome;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the JSON-HOME resource as defined in the
 * <a href="http://tools.ietf.org/html/draft-nottingham-json-home-03#section-2">IETF draft</a>.
 * <p>
 * The JSON-HOME document contains a "resources" property with all the application's entry points.
 * </p>
 * For example:
 * <pre>
 * GET / HTTP/1.1
 * Host: example.org
 * Accept: application/json-home
 *
 * HTTP/1.1 200 OK
 * Content-Type: application/json-home
 * Cache-Control: max-age=3600
 * Connection: close
 *
 * {
 *   "resources": {
 *     "http://example.org/rel/widgets": {
 *       "href": "/widgets/"
 *     },
 *     "http://example.org/rel/widget": {
 *       "href-template": "/widgets/{widget_id}",
 *       "href-vars": {
 *         "widget_id": "http://example.org/param/widget"
 *       },
 *       "hints": {
 *         "allow": ["GET", "PUT", "DELETE", "PATCH"],
 *         "formats": {
 *           "application/json": {}
 *         },
 *         "accept-patch": ["application/json-patch"],
 *         "accept-post": ["application/xml"],
 *         "accept-ranges": ["bytes"]
 *       }
 *     }
 *   }
 * }
 * </pre>
 */
public class JsonHome {

    private final Map<String, Map<String, Object>> resources = new HashMap<>();

    public JsonHome(Map<String, Resource> resourceMap) {
        for (Map.Entry<String, Resource> resourceEntry : resourceMap.entrySet()) {
            String rel = resourceEntry.getKey();
            Resource resource = resourceEntry.getValue();
            // flatten the resource
            resources.put(rel, resource.toRepresentation());
        }
    }

    public Map<String, Map<String, Object>> getResources() {
        return resources;
    }
}
