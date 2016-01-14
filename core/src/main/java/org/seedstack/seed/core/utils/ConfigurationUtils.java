/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

/**
 * Provides utils method for common configuration.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public final class ConfigurationUtils {

    private ConfigurationUtils() {}

    /**
     * Builds a configuration key. All the fragments will be concatenated with the "." separator.
     * If
     *
     * @param keyFragments key fragments
     * @return the built key
     */
    public static String buildKey(String... keyFragments) {
        StringBuilder sb = new StringBuilder();
        for (String keyFragment1 : keyFragments) {
            String keyFragment = keyFragment1;
            if (keyFragment.startsWith(".")) {
                keyFragment = keyFragment.substring(1);
            }
            // test if a separator is present
            if (keyFragment.endsWith(".")) {
                sb.append(keyFragment);
            } else {
                sb.append(keyFragment).append(".");
            }
        }
        // strip the last separator
        String concatenatedKey = sb.toString();
        concatenatedKey = concatenatedKey.substring(0, concatenatedKey.length() - 1);
        return concatenatedKey;
    }
}
