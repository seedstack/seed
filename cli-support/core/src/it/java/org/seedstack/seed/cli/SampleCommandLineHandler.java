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

import org.seedstack.seed.cli.spi.CliArgs;
import org.seedstack.seed.cli.spi.CliOption;
import org.seedstack.seed.cli.spi.CommandLineHandler;
import io.nuun.plugin.cli.NuunOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author epo.jemba@ext.mpsa.com
 */
public class SampleCommandLineHandler implements CommandLineHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleCommandLineHandler.class);

    @CliOption(opt = "a", arg = false)
    private Boolean hasA;

    @NuunOption(opt = "b", arg = true)
    private String b;

    @NuunOption(opt = "P", args = true, valueSeparator = '=')
    private String P[];

    @CliArgs
    private String[] arg;


    @Override
    public String name() {
        return "Test Application Handler";
    }

    @Override
    public Integer call() throws Exception {

        LOGGER.info("EXECUTING !!");
        LOGGER.info("hasA = " + hasA);
        LOGGER.info("b = " + b);
        if (arg != null) {
            LOGGER.info("arg.length = " + arg.length);
            LOGGER.info("arg = " + arg[0]);
        }


        assertThat(hasA).isNotNull();
        assertThat(hasA).isTrue();

        assertThat(b).isNotNull();
        assertThat(b).isEqualTo("babar");

        assertThat(arg).isNotNull();
        assertThat(arg).containsExactly("zob");

        assertThat(P).isNotNull();
        assertThat(P).containsExactly("key1", "value1", "key2", "value2");

        return 0;
    }
}
