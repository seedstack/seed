/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.testing.ConfigurationProfiles;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(SeedITRunner.class)
public class LoggingIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingIT.class);
    private static final File LOGFILE = new File("test.log");

    @BeforeClass
    public static void setUp() throws Exception {
        System.clearProperty("testfileLogging");
        if (LOGFILE.exists() && !LOGFILE.delete()) {
            throw new IllegalStateException("Unable to delete test file");
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.clearProperty("testfileLogging");
        Seed.refresh();
        if (LOGFILE.exists() && !LOGFILE.delete()) {
            throw new IllegalStateException("Unable to delete test file");
        }
    }

    @Test
    @ConfigurationProfiles("fileLogging")
    public void fileOutputIsWorking() {
        LOGGER.info("Yop");
        assertThat(LOGFILE).doesNotExist();
        System.setProperty("testfileLogging", "true");
        Seed.refresh();
        LOGGER.info("Yop");
        assertThat(LOGFILE).matches(f -> {
            try {
                return Files.readAllLines(f.toPath()).stream().anyMatch(l -> l.contains("Yop"));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
