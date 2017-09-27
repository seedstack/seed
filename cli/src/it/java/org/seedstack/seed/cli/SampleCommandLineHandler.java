/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

@CliCommand("test")
public class SampleCommandLineHandler implements CommandLineHandler {
    static boolean called = false;

    @CliOption(name = "a")
    private Boolean hasA;

    @CliOption(name = "b", valueCount = 1)
    private String b;

    @CliOption(name = "P", valueCount = -1, valueSeparator = '=')
    private Map<String, String> P1;

    @CliOption(name = "P", valueCount = -1, valueSeparator = '=')
    private String[] P2;

    @CliOption(name = "A", valueCount = -1, mandatory = true, defaultValues = "2")
    private String[] A1;

    @CliOption(name = "A", valueCount = -1, mandatory = true, defaultValues = "2")
    private String A2;

    @CliOption(name = "B", valueCount = 2, mandatoryValue = true, defaultValues = {"2", "3"})
    private String[] B;

    @CliOption(name = "C", valueCount = 5)
    private String[] C;

    @CliOption(name = "D", valueCount = 5)
    private String D;

    @CliArgs(mandatoryCount = 1)
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

        assertThat(A1).isNotNull();
        assertThat(A1).containsExactly("5", "6", "7", "8", "5", "4", "5", "6");

        assertThat(A2).isNotNull();
        assertThat(A2).isEqualTo("5");

        assertThat(B).isNotNull();
        assertThat(B).containsExactly("2", "3");

        assertThat(C).isNull();

        assertThat(D).isNull();

        called = true;

        return 0;
    }
}
