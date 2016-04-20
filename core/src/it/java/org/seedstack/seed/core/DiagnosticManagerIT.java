/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import io.nuun.kernel.api.Kernel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seedstack.seed.DiagnosticManager;
import org.seedstack.seed.ErrorCode;
import org.seedstack.seed.SeedException;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DiagnosticManagerIT {
    private static Kernel kernel;
    private static Holder holder;

    static class Holder {
        @Inject
        DiagnosticManager diagnosticManager;
    }

    @Test
    public void diagnostic_manager_is_injected() {
        assertThat(holder).isNotNull();
        assertThat(holder.diagnosticManager).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void diagnostic_system_information_is_present() {
        Map<String, Object> diagnosticInfo = holder.diagnosticManager.getDiagnosticInfo(null);

        assertThat(diagnosticInfo).isNotNull();
        Map<String, Object> systemInfo = (Map<String, Object>) diagnosticInfo.get("system");

        assertThat(systemInfo).isNotNull();
        assertThat((Integer) systemInfo.get("processors")).isGreaterThan(0);
        assertThat((Map<String, Long>) systemInfo.get("memory")).isNotEmpty();
        assertThat((List<String>) systemInfo.get("args")).isNotNull();
        assertThat(systemInfo.get("start-time")).isNotNull();
        assertThat(systemInfo.get("diagnostic-time")).isNotNull();
        assertThat((Map<String, String>) systemInfo.get("properties")).isNotEmpty();
        assertThat((Map<Long, Object>) systemInfo.get("threads")).isNotEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void seed_info_is_present() {
        Map<String, Object> diagnosticInfo = holder.diagnosticManager.getDiagnosticInfo(null);

        assertThat(diagnosticInfo).isNotNull();

        Map<String, Object> seedInfo = (Map<String, Object>) diagnosticInfo.get("seed");
        assertThat((Set<URL>)(seedInfo.get("scanned-urls"))).isNotEmpty();
        assertThat((Boolean)(seedInfo.get("color-output"))).isNotNull();
    }


    @Test
    @SuppressWarnings("unchecked")
    public void diagnostic_exception_information_is_present() {
        SeedException seedException = SeedException.createNew(TestErrorCode.TEST_CODE);
        Map<String, Object> diagnosticInfo = holder.diagnosticManager.getDiagnosticInfo(seedException);

        assertThat(diagnosticInfo).isNotNull();

        Map<String, Object> exceptionInfo = (Map<String, Object>) diagnosticInfo.get("exception");
        assertThat(exceptionInfo.get("message")).isEqualTo(seedException.getMessage());
        assertThat(exceptionInfo.get("description")).isEqualTo(seedException.getDescription());
        assertThat(exceptionInfo.get("fix")).isNull();
        assertThat(exceptionInfo.get("class")).isEqualTo(seedException.getClass().getCanonicalName());
        assertThat((List<String>) exceptionInfo.get("stacktrace")).isNotEmpty();
        assertThat((List<Object>) exceptionInfo.get("causes")).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void diagnostic_application_information_is_present() {
        Map<String, Object> diagnosticInfo = holder.diagnosticManager.getDiagnosticInfo(null);

        assertThat(diagnosticInfo).isNotNull();
        Map<String, Object> applicationInfo = (Map<String, Object>) diagnosticInfo.get("org.seedstack.seed.core.application");

        assertThat(applicationInfo).isNotNull();
        assertThat(applicationInfo.get("id")).isNotNull();
        assertThat(applicationInfo.get("name")).isNotNull();
        assertThat(applicationInfo.get("version")).isNotNull();
        assertThat(applicationInfo.get("storage-location")).isNull();
        assertThat(applicationInfo.get("base-packages")).isNotNull();
        assertThat(applicationInfo.get("active-profiles")).isNotNull();
        assertThat(applicationInfo.get("configuration")).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void diagnostic_information_from_scanned_collectors_is_present() {
        Map<String, Object> diagnosticInfo = holder.diagnosticManager.getDiagnosticInfo(null);

        assertThat(diagnosticInfo).isNotNull();
        Map<String, Object> testInfo = (Map<String, Object>) diagnosticInfo.get("test");

        assertThat(testInfo).isNotNull();
        assertThat(testInfo.get("service")).isNotNull();
    }

    @BeforeClass
    public static void setup() {
        kernel = Seed.createKernel();
        holder = kernel.objectGraph().as(Injector.class).createChildInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Holder.class);
            }
        }).getInstance(Holder.class);
    }

    @AfterClass
    public static void teardown() {
        Seed.disposeKernel(kernel);
    }

    private enum TestErrorCode implements ErrorCode {
        TEST_CODE
    }
}
