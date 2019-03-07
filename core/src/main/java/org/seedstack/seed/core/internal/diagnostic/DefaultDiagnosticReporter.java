/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.diagnostic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.seedstack.seed.diagnostic.spi.DiagnosticReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link DiagnosticReporter} that logs to a JSON
 * file in the system temporary directory (from java.io.tmpdir system property).
 */
class DefaultDiagnosticReporter implements DiagnosticReporter {
    private static final String DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss.SSS";
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDiagnosticReporter.class);
    private static final YAMLFactory YAML_FACTORY;
    private static final DefaultPrettyPrinter DEFAULT_PRETTY_PRINTER;

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(
                JsonInclude.Include.ALWAYS,
                JsonInclude.Include.NON_NULL
        ));
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        YAML_FACTORY = new YAMLFactory();
        YAML_FACTORY.setCodec(new ObjectMapper());

        DEFAULT_PRETTY_PRINTER = new DefaultPrettyPrinter();
        DEFAULT_PRETTY_PRINTER.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
    }

    @Override
    public void writeDiagnosticReport(Map<String, Object> diagnosticInfo) throws IOException {
        File diagnosticFile;

        File diagnosticDirectory = new File(System.getProperty("java.io.tmpdir"), "seedstack-diagnostics");
        if (!diagnosticDirectory.exists() && !diagnosticDirectory.mkdirs() || !diagnosticDirectory.isDirectory() ||
                !diagnosticDirectory.canWrite()) {
            diagnosticDirectory = new File(System.getProperty("java.io.tmpdir"));
        }

        diagnosticFile = new File(diagnosticDirectory, String.format("seedstack-diagnostic-%s.yaml",
                new SimpleDateFormat(DefaultDiagnosticReporter.DATE_FORMAT).format(new Date())));
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(diagnosticFile),
                Charset.forName("UTF-8").newEncoder())) {
            writeDiagnosticReport(diagnosticInfo, writer);
            LOGGER.warn("Diagnostic information dumped to file://{}", diagnosticFile.toURI().toURL().getPath());
        } catch (RuntimeException e) {
            LOGGER.warn("Unable to write diagnostic report", e);
        }
    }

    void writeDiagnosticReport(Map<String, Object> diagnosticInfo, Writer writer) throws IOException {
        try (JsonGenerator jsonGenerator = YAML_FACTORY.createGenerator(writer)) {
            jsonGenerator.setPrettyPrinter(DEFAULT_PRETTY_PRINTER);
            jsonGenerator.writeObject(diagnosticInfo);
            jsonGenerator.flush();
        }
    }
}
