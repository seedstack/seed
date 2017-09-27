/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.seedstack.seed.diagnostic.DiagnosticManager;
import org.seedstack.seed.it.AbstractSeedWebIT;

public class WebDiagnosticsIT extends AbstractSeedWebIT {
    @Inject
    private DiagnosticManager diagnosticManager;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Test
    @RunAsClient
    @SuppressWarnings("unchecked")
    public void web_diagnostic_information_is_present() throws Exception {
        Map<String, Object> diagnosticInfo = diagnosticManager.getDiagnosticInfo(null);

        assertThat(diagnosticInfo).isNotNull();
        Map<String, Object> webInfo = (Map<String, Object>) diagnosticInfo.get("web");

        assertThat(webInfo).isNotEmpty();
    }
}
