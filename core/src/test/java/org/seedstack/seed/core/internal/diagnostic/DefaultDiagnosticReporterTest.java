/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.diagnostic;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultDiagnosticReporterTest {
    private DefaultDiagnosticReporter underTest = new DefaultDiagnosticReporter();

    @Test
    public void json_output_is_valid() throws Exception {
        assertThat(produceReport()).isEqualTo("---\nhello:\n  subHello: \"subWorld\"\n");
    }

    @Test
    public void json_output_is_pretty() throws Exception {
        assertThat(produceReport()).contains("\n");
    }

    private String produceReport() throws IOException {
        HashMap<String, Object> diagnostics = new HashMap<>();
        HashMap<String, Object> subMapDiagnostics = new HashMap<>();
        subMapDiagnostics.put("subHello", "subWorld");

        diagnostics.put("hello", "world");
        diagnostics.put("hello", subMapDiagnostics);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        underTest.writeDiagnosticReport(diagnostics, baos);
        return baos.toString("utf-8");
    }
}
