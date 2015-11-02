/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class ConfigurationUtilsTest {

    @Test
    public void test_build_config_key() {
        String key = ConfigurationUtils.buildKey("foo", "bar");
        Assertions.assertThat(key).isEqualTo("foo.bar");

        key = ConfigurationUtils.buildKey("foo.", "bar"); // concatenate fragments with separator
        Assertions.assertThat(key).isEqualTo("foo.bar");

        key = ConfigurationUtils.buildKey(".foo", ".bar"); // strip the first separator
        Assertions.assertThat(key).isEqualTo("foo.bar");

        key = ConfigurationUtils.buildKey("foo", "bar."); // strip the last separator
        Assertions.assertThat(key).isEqualTo("foo.bar");
    }
}
