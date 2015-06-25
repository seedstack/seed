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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
class UriBuilder {

    private static final Pattern JAX_RS_TEMPLATE_PARAMETERS = Pattern.compile("\\{(\\w[\\w\\.-]*): .+?\\}");
    private static final Pattern JAX_RS_TEMPLATE = Pattern.compile(".*\\{(\\w[\\w\\.-]*)(: .+?)?\\}.*");

    private UriBuilder() {
    }

    /**
     * Constructs a URI with the base and the paths. Slashes in the
     * parameters don't matter. The final slash will always be removed.
     * <p>
     * No encoding or replacement will be done.
     * </p>
     * The base parameter should not be blank, but the paths can be null
     * or empty, in this case they wont be added.
     *
     * @param base  the base path
     * @param paths the paths
     * @return the built path
     */
    public static String path(String base, final String... paths) {
        if (base == null || base.isEmpty()) {
            throw new IllegalArgumentException("The base path should not be null or empty");
        }
        StringBuilder sb = new StringBuilder().append(stripLeadingSlash(base));
        for (String s : paths) {
            if (s == null || s.isEmpty()) {
                continue;
            }
            if (!s.startsWith("/")) {
                sb.append("/");
            }
            sb.append(stripLeadingSlash(s));
        }
        return sb.toString();
    }

    /**
     * Removes the leading slash in the given path.
     *
     * @param path the path
     * @return the new path
     */
    public static String stripLeadingSlash(String path) {
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
    public static String stripJaxRsRegex(String hrefTemplate) {
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
    public static boolean jaxTemplate(String href) {
        Matcher m = JAX_RS_TEMPLATE.matcher(href);
        return m.matches();
    }
}
