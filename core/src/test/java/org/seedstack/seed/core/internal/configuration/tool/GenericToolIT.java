/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration.tool;

import org.junit.Ignore;
import org.junit.Test;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.ToolLauncher;
import org.seedstack.seed.core.internal.configuration.tool.fixtures.ExitException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Ignore("Cannot be tested like this after Java 17")
public class GenericToolIT extends AbstractToolIT {

    @Test
    public void nonExistentSeedTool() {
        ToolLauncher launcher = new ToolLauncher("non-existentSeedTool");
        assertThatExceptionOfType(SeedException.class).isThrownBy(() -> launcher.launch(new String[]{"jta"}));
    }

    @Test
    public void validConfigToolWithParameters() throws Exception {
        ToolLauncher launcher = new ToolLauncher("config");
        try {
            launcher.launch(new String[]{"transaction.jta"});
        } catch (ExitException e) {
            assertThat(output.toString()).contains("Configuration options");
        }
    }

    @Test
    public void validConfigToolWithoutParameters() throws Exception {
        ToolLauncher launcher = new ToolLauncher("config");
        try {
            launcher.launch(new String[]{});
        } catch (ExitException e) {
            assertThat(output.toString()).contains("Configuration options");
        }
    }

}