/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Singleton;
import org.seedstack.seed.Bind;

@CliCommand("dummy")
@Bind
@Singleton
public class DummyCommandLineHandler implements CommandLineHandler {
    boolean called = false;

    @CliOption(name = "o", longName = "option", valueCount = 1)
    private String option;

    @CliArgs
    private String[] args;

    @Override
    public Integer call() {
        assertThat(args.length).isEqualTo(2);
        assertThat(args[0]).isEqualTo("arg0");
        assertThat(args[1]).isEqualTo("arg1");
        assertThat(option).isEqualTo("value");
        called = true;
        return 255;
    }
}
