/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Various String utilities.
 *
 * @author adrien.lauer@mpsa.com
 */
public final class SeedStringUtils {

    private SeedStringUtils() {
    }

    /**
     * Replace ${...} placeholders in a string looking up in a replacement map.
     *
     * @param text the text to replace.
     * @param replacements the map of replacements.
     * @return the replaced text.
     */
    public static String replaceTokens(String text, Map<String, Object> replacements) {
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            Object replacement = replacements.get(matcher.group(1));
            matcher.appendReplacement(buffer, "");

            if (replacement != null) {
                buffer.append(replacement.toString());
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
