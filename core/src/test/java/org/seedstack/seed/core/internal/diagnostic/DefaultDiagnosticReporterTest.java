/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.diagnostic;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import org.junit.Test;

public class DefaultDiagnosticReporterTest {
    private DefaultDiagnosticReporter underTest = new DefaultDiagnosticReporter();

    @Test
    public void yaml_output_is_valid() throws Exception {
        assertThat(produceReport()).isEqualTo("---\nhello:\n  subHello: \"subWorld\"\n");
    }

    @Test
    public void yaml_output_is_pretty() throws Exception {
        assertThat(produceReport()).contains("\n");
    }

    private String produceReport() throws IOException {
        HashMap<String, Object> diagnostics = new HashMap<>();
        HashMap<String, Object> subMapDiagnostics = new HashMap<>();
        subMapDiagnostics.put("subHello", "subWorld");

        diagnostics.put("hello", "world");
        diagnostics.put("hello", subMapDiagnostics);

        StringWriter stringWriter = new StringWriter();
        underTest.writeDiagnosticReport(diagnostics, stringWriter);
        return stringWriter.toString();
    }
}
