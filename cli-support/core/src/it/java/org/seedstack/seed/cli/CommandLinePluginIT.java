/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandLinePluginIT {
    @Test
    public void test() throws Exception {
        SeedRunner.execute(new String[]{"-a", "-b", "babar", "zob", "-P", "key1=value1", "-Pkey2=value2", "-A", "5,6,7,8,5,4,5,6", "-C"});
        assertThat(SampleCommandLineHandler.called).isTrue();
        assertThat(UnusedCommandLineHandler.called).isFalse();
    }
}
