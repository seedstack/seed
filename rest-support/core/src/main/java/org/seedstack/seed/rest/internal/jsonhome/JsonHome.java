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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class JsonHome {

    private final Map<String, Map<String, Object>> resources = new HashMap<String, Map<String, Object>>();

    public JsonHome(String baseRel, String baseParam, Collection<Class<?>> resourceClasses) {
        Map<String, Resource> resourceMap = new ResourceParser(baseRel, baseParam).parse(resourceClasses);

        for (Map.Entry<String, Resource> resourceEntry : resourceMap.entrySet()) {
            String rel = resourceEntry.getKey();
            Resource resource = resourceEntry.getValue();
            resources.put(rel, resource.toRepresentation());
        }
    }

    public Map<String, Map<String, Object>> getResources() {
        return resources;
    }
}
