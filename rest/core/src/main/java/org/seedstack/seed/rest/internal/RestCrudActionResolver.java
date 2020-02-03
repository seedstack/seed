/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.seedstack.seed.security.CrudAction;
import org.seedstack.seed.security.spi.CrudActionResolver;

class RestCrudActionResolver implements CrudActionResolver {
    private final Map<Class<? extends Annotation>, CrudAction> annotationMap;

    RestCrudActionResolver() {
        Map<Class<? extends Annotation>, CrudAction> map = new HashMap<>();
        map.put(javax.ws.rs.DELETE.class, CrudAction.DELETE);
        map.put(javax.ws.rs.GET.class, CrudAction.READ);
        map.put(javax.ws.rs.HEAD.class, CrudAction.READ);
        map.put(javax.ws.rs.OPTIONS.class, CrudAction.READ);
        map.put(javax.ws.rs.POST.class, CrudAction.CREATE);
        map.put(javax.ws.rs.PUT.class, CrudAction.UPDATE);
        annotationMap = Collections.unmodifiableMap(map);
    }

    @Override
    public Optional<CrudAction> resolve(Method method) {
        return Arrays.stream(method.getAnnotations())
                .map(Annotation::annotationType)
                .map(x -> annotationMap.getOrDefault(x, null))
                .filter(Objects::nonNull)
                .findFirst();
    }
}
