/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class UriBuilder {

    private static final Pattern JAX_RS_TEMPLATE_PARAMETERS = Pattern.compile(
            "\\{\\s*(\\w[\\w\\.-]*)\\s*:\\s*.+?\\s*\\}");
    private static final Pattern JAX_RS_TEMPLATE = Pattern.compile(".*\\{(\\w[\\w\\.-]*)(: .+?)?\\}.*");

    private UriBuilder() {
    }

    /**
     * Constructs a URI. The final slash will always be removed.
     * <p>
     * No encoding or replacement will be done.
     * </p>
     * The paths can be null or empty, in this case they wont be added.
     *
     * @param paths the paths
     * @return the built path
     */
    static String uri(final String... paths) {
        StringBuilder sb = new StringBuilder();
        boolean firstPath = true;
        for (String s : paths) {
            if (s == null || s.isEmpty()) {
                continue;
            }
            if (!s.startsWith("/") && !firstPath) {
                sb.append("/");
            }
            sb.append(stripLeadingSlash(s));
            firstPath = false;
        }
        return sb.toString();
    }

    /**
     * Removes the leading slash in the given path.
     *
     * @param path the path
     * @return the new path
     */
    static String stripLeadingSlash(String path) {
        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        } else {
            return path;
        }
    }

    /**
     * Replaces the JAX-RS regex in the path by a URI template level 1.
     * <pre>
     * /widgets/{widgetName: [a-zA-Z][a-zA-Z_0-9]}
     * </pre>
     * Become:
     * <pre>
     * /widgets/{widgetName}
     * </pre>
     *
     * @param hrefTemplate the href template
     * @return the new href template
     */
    static String stripJaxRsRegex(String hrefTemplate) {
        Matcher m = JAX_RS_TEMPLATE_PARAMETERS.matcher(hrefTemplate);

        StringBuffer newHref = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(newHref, "{" + m.group(1) + "}");
        }
        m.appendTail(newHref);

        return newHref.toString();
    }

    /**
     * Returns whether the href is a JAX-RS template.
     *
     * @param href the href to check
     * @return true if the href is templated, false otherwise
     */
    static boolean jaxTemplate(String href) {
        Matcher m = JAX_RS_TEMPLATE.matcher(href);
        return m.matches();
    }
}
