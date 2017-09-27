/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.it;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import org.junit.Test;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.it.fixtures.TestKernelRule;
import org.seedstack.seed.it.fixtures.WithTestAnnotation;

public class NoKernelModeIT extends AbstractSeedIT {
    @Inject
    TestKernelRule testKernelRule;

    @Configuration(value = "testKey")
    private String testKey;

    @Configuration(value = "foo1", injectDefault = false)
    private String foo1;

    @Configuration(value = "foo2")
    private String foo2;

    @Test
    @WithTestAnnotation(key = "foo1", value = "bar1")
    public void method_annotation_is_used_by_rule_1() {
        assertThat(testKernelRule).isNotNull();
        assertThat(testKey).isEqualTo("testValue");
        assertThat(foo1).isEqualTo("bar1");
    }

    @Test
    @WithTestAnnotation(key = "foo2", value = "bar2")
    public void method_annotation_is_used_by_rule_2() {
        assertThat(testKernelRule).isNotNull();
        assertThat(testKey).isEqualTo("testValue");
        assertThat(foo1).isNull();
        assertThat(foo2).isEqualTo("bar2");
    }
}
