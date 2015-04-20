/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.seedstack.seed.core.api.DiagnosticManager;
import org.seedstack.seed.it.AbstractSeedWebIT;
import org.seedstack.seed.web.internal.WebPlugin;

import javax.inject.Inject;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class WebDiagnosticsIT extends AbstractSeedWebIT {
    @Inject
    DiagnosticManager diagnosticManager;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).setWebXML("WEB-INF/web.xml");
    }

    @Test
    @RunAsClient
    @SuppressWarnings("unchecked")
    public void web_diagnostic_information_is_present() throws Exception {
        Map<String, Object> diagnosticInfo = diagnosticManager.getDiagnosticInfo(null);

        assertThat(diagnosticInfo).isNotNull();
        Map<String, Object> webInfo = (Map<String, Object>) diagnosticInfo.get(WebPlugin.WEB_PLUGIN_PREFIX);

        assertThat(webInfo).isNotEmpty();
    }
}
