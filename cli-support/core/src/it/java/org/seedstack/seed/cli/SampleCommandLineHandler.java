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

import org.seedstack.seed.cli.api.CliArgs;
import org.seedstack.seed.cli.api.CliCommand;
import org.seedstack.seed.cli.api.CliOption;
import org.seedstack.seed.cli.api.CommandLineHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author epo.jemba@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 */
@CliCommand("test")
public class SampleCommandLineHandler implements CommandLineHandler {
    static boolean called = false;

    @CliOption(name = "a")
    private Boolean hasA;

    @CliOption(name = "b", valueCount = 1)
    private String b;

    @CliOption(name = "P", valueCount = -1)
    private Map<String, String> P1;

    @CliOption(name = "P", valueCount = -1)
    private String[] P2;

    @CliArgs
    private String[] args;

    @Override
    public Integer call() throws Exception {
        assertThat(hasA).isNotNull();
        assertThat(hasA).isTrue();

        assertThat(b).isNotNull();
        assertThat(b).isEqualTo("babar");

        assertThat(args).isNotNull();
        assertThat(args).containsExactly("zob");

        assertThat(P1).isNotNull();
        assertThat(P1.get("key1")).isEqualTo("value1");
        assertThat(P1.get("key2")).isEqualTo("value2");

        assertThat(P2).isNotNull();
        assertThat(P2).containsExactly("key1", "value1", "key2", "value2");

        called = true;

        return 0;
    }
}
