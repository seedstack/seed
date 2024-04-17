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
import org.seedstack.coffig.Config;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.core.internal.ToolLauncher;
import org.seedstack.seed.core.internal.configuration.tool.fixtures.Credentials;
import org.seedstack.seed.core.internal.configuration.tool.fixtures.ExitException;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@Ignore("Cannot be tested like this after Java 17")
public class CheckToolIT extends AbstractToolIT {

    @Test
    public void runCheckToolInStandardMode() throws Exception {
        ToolLauncher launcher = new ToolLauncher("check");
        try {
            launcher.launch(new String[]{});
        } catch (ExitException e) {
            runWarningChecks();
        }
    }

    @Test
    public void runCheckToolInVerboseMode() throws Exception {
        ToolLauncher launcher = new ToolLauncher("check");
        try {
            launcher.launch(new String[]{"-v"});
        } catch (ExitException e) {
            runInfoChecks();
            runWarningChecks();
        }
    }

    @Test
    public void runCheckToolInLongVerboseMode() throws Exception {
        ToolLauncher launcher = new ToolLauncher("check");
        try {
            launcher.launch(new String[]{"--verbose"});
        } catch (ExitException e) {
            runInfoChecks();
            runWarningChecks();
        }
    }

    private void runInfoChecks() {
        // Basic properties are correctly analyzed
        assertThat(output.toString()).contains(new CheckTool.EntrySearchResult(CheckTool.SeverityEnum.INFO, "someObject.property1", CheckTool.StatusEnum.FOUND, org.seedstack.seed.core.ConfigurationIT.ConfigObject.class).toString());
        // Maps are corretly analyzed
        assertThat(output.toString()).contains(new CheckTool.EntrySearchResult(CheckTool.SeverityEnum.INFO, "jndi.additionalContexts", CheckTool.StatusEnum.FOUND, org.seedstack.seed.JndiConfig.class).toString());
        // Arrays of primitives are correctly analyzed
        assertThat(output.toString()).contains(new CheckTool.EntrySearchResult(CheckTool.SeverityEnum.INFO, "application.basePackages", CheckTool.StatusEnum.FOUND, org.seedstack.seed.ApplicationConfig.class).toString());
        // Arrays of user objects are correctly analyzed
        assertThat(output.toString()).contains(new CheckTool.EntrySearchResult(CheckTool.SeverityEnum.INFO, "arrayOfMapContainer.credentials.username", CheckTool.StatusEnum.FOUND, org.seedstack.seed.core.internal.configuration.tool.fixtures.Credentials.class).toString());
        // Configuration annotations are correctly analyzed
        assertThat(output.toString()).contains(new CheckTool.EntrySearchResult(CheckTool.SeverityEnum.INFO, "entry.for.configurationAnnotation", CheckTool.StatusEnum.FOUND, CheckToolIT.class).toString());
        assertThat(output.toString()).contains(new CheckTool.EntrySearchResult(CheckTool.SeverityEnum.INFO, "firstLevelEntry", CheckTool.StatusEnum.FOUND, CheckToolIT.class).toString());
    }

    private void runWarningChecks() {
        // Property used through programmatic mechanisms will not be found
        assertThat(output.toString()).contains(new CheckTool.EntrySearchResult(CheckTool.SeverityEnum.WARNING, "person1", CheckTool.StatusEnum.NOT_FOUND).toString());
        // Arrays of Maps are not yet recognized, neither by Coffig neither by the tool
        assertThat(output.toString()).contains(new CheckTool.EntrySearchResult(CheckTool.SeverityEnum.WARNING, "arrayOfMapContainer.arrayOfMaps.entry0.username", CheckTool.StatusEnum.NOT_FOUND).toString());
    }

    @Config("arrayOfMapContainer")
    public static class AnonymousMapContainer {
        private String basicProperty;
        private Map<String, Credentials> arrayOfMaps[];
        private Credentials[] credentials;

        String getBasicProperty() {
            return basicProperty;
        }

        Map<String, Credentials>[] getArrayOfMaps() {
            return arrayOfMaps;
        }

        Credentials[] getCredentials() {
            return credentials;
        }
    }

    @Configuration
    private AnonymousMapContainer anonymousMapContainer;

    @Configuration("entry.for.configurationAnnotation")
    private String configurationAnnotation;

    @Configuration("firstLevelEntry")
    private String[] someProperty;
}