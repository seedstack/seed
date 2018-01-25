/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web;

import java.util.Map;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.diagnostic.DiagnosticManager;

@RunWith(Arquillian.class)
public class WebDiagnosticsIT {
    @Inject
    private DiagnosticManager diagnosticManager;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    @RunAsClient
    public void webDiagnosticInformationIsPresent() {
        Map<String, Object> diagnosticInfo = diagnosticManager.getDiagnosticInfo(null);

        Assertions.assertThat(diagnosticInfo).isNotNull();
        Map<String, Object> webInfo = (Map<String, Object>) diagnosticInfo.get("web");

        Assertions.assertThat(webInfo).isNotEmpty();
    }
}
