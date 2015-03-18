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

import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author epo.jemba@ext.mpsa.com
 */
public class CommandLinePluginIT {

    private String[] provideCommandLine() {
        return new String[]{"-a", "-b", "babar", "zob", "-Pkey1=value1", "-Pkey2=value2"};
    }

    @Test
    public void test() throws Exception {
        assertThat(SeedRunner.execute(provideCommandLine(), new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 12;
            }
        })).isEqualTo(12);
    }

}
