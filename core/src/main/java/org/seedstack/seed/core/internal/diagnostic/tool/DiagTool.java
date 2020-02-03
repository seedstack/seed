/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.diagnostic.tool;

import com.google.common.base.Strings;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import org.seedstack.seed.cli.CliOption;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.core.internal.AbstractSeedTool;

public class DiagTool extends AbstractSeedTool {
    @CliOption(name = "f", longName = "file", valueCount = 1, mandatoryValue = true)
    private String file;

    @Override
    public String toolName() {
        return "diag";
    }

    @Override
    public Integer call() throws Exception {
        if (Strings.isNullOrEmpty(file)) {
            try (Writer writer = new OutputStreamWriter(System.out, Charset.forName("UTF-8").newEncoder())) {
                return write(writer);
            }
        } else {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file),
                    Charset.forName("UTF-8").newEncoder())) {
                return write(writer);
            }
        }
    }

    private Integer write(Writer writer) {
        try {
            Seed.diagnostic().writeDiagnosticReport(null, writer);
            return 0;
        } catch (Exception e) {
            System.err.println("Failed to create the diagnostic report");
            Seed.translateException(e).printStackTrace(System.err);
            return 1;
        }
    }
}
