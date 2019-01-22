/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.jsonhome;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.ws.rs.Consumes;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Produces;

/**
 * Scans methods for JSON-HOME hints.
 */
public class HintScanner {

    /**
     * Finds the JSON-HOME hints on the given method.
     *
     * @param method the method to scan
     * @return the hints
     */
    public Hints findHint(Method method) {
        Hints hints = new Hints();
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            findAllow(hints, annotation);
            findFormats(hints, annotation);
        }
        return hints;
    }

    private void findAllow(Hints hints, Annotation annotation) {
        HttpMethod httpMethod = annotation.annotationType().getAnnotation(HttpMethod.class);
        if (httpMethod != null) {
            hints.addAllow(httpMethod.value());
        }
    }

    private void findFormats(Hints hints, Annotation annotation) {
        if (annotation.annotationType().equals(Consumes.class)) {
            for (String mediaType : ((Consumes) annotation).value()) {
                hints.format(mediaType, "");
            }
        }
        if (annotation.annotationType().equals(Produces.class)) {
            for (String mediaType : ((Produces) annotation).value()) {
                hints.format(mediaType, "");
            }
        }
    }
}
