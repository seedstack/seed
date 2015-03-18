/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.seedstack.seed.core.api.DiagnosticManager;
import org.seedstack.seed.core.api.ErrorCode;
import org.seedstack.seed.core.api.SeedException;
import io.nuun.kernel.api.Kernel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static io.nuun.kernel.core.NuunCore.createKernel;
import static io.nuun.kernel.core.NuunCore.newKernelConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

public class DiagnosticManagerIT {

    static Kernel underTest;
    static Holder holder;

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
        assertThat((List<String>) systemInfo.get("classpath")).isNotEmpty();
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
    public void real_scanned_classpath_can_be_detected() {
        Map<String, Object> diagnosticInfo = holder.diagnosticManager.getDiagnosticInfo(null);

        assertThat(diagnosticInfo).isNotNull();
        Map<String, Object> systemInfo = (Map<String, Object>) diagnosticInfo.get("system");

        assertThat(systemInfo.get("classpath-info")).isEqualTo("Classpath is based on the classpath scanned by SEED");
    }


    @Test
    @SuppressWarnings("unchecked")
    public void diagnostic_exception_information_is_present() {
        SeedException seedException = SeedException.createNew(TestErrorCode.TEST_CODE);
        Map<String, Object> diagnosticInfo = holder.diagnosticManager.getDiagnosticInfo(seedException);

        assertThat(diagnosticInfo).isNotNull();

        Map<String, Object> exceptionInfo = (Map<String, Object>) diagnosticInfo.get("exception");
        assertThat(exceptionInfo.get("message")).isEqualTo(seedException.getMessage());
        assertThat(exceptionInfo.get("seed-message")).isEqualTo(seedException.getSeedMessage());
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
        assertThat(applicationInfo.get("configuration")).isNotNull();
    }

    @BeforeClass
    public static void setup() {
        underTest = createKernel(newKernelConfiguration());
        underTest.init();
        underTest.start();

        Module aggregationModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Holder.class);
            }
        };

        holder = underTest.objectGraph().as(Injector.class).createChildInjector(aggregationModule).getInstance(Holder.class);
    }

    @AfterClass
    public static void teardown() {
        underTest.stop();
        underTest = null;
        holder = null;
    }

    private enum TestErrorCode implements ErrorCode {
        TEST_CODE
    }
}
