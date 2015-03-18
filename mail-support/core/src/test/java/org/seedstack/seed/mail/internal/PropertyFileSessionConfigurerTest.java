/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.mail.internal;

import org.seedstack.seed.mail.spi.SessionConfigurer;
import jodd.props.Props;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import javax.mail.Session;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(BlockJUnit4ClassRunner.class)
public class PropertyFileSessionConfigurerTest {
    private Map<String, Session> sessionsConfig;

    @Before
    public void setUp() throws Exception {
        Props props = new Props();
        Map<String, Object> config = new HashMap<String, Object>();
        props.load(PropertyFileSessionConfigurerTest.class.getResourceAsStream("/test.props"));
        props.extractBaseProps(config);
        Configuration configuration = new MapConfiguration(config);
        assertThat(config).isNotEmpty();
        assertThat(configuration).isNotNull();

        SessionConfigurer configurer = new PropertyFileSessionConfigurer(configuration.subset(JavaMailPlugin.CONFIGURATION_PREFIX));
        this.sessionsConfig = configurer.doConfigure();
    }

    @Test
    public void testDoConfigure() throws Exception {
        assertThat(sessionsConfig).isNotNull();
        assertThat(sessionsConfig).isNotEmpty();
        assertThat(sessionsConfig).hasSize(4);
    }


    @Test
    public void test_smtp_session_configuration_is_present() {
        assertThat(sessionsConfig).containsKey("smtp");
        final Session smtp = sessionsConfig.get("smtp");
        assertThat(smtp).isNotNull();
        assertThat(smtp.getProperty("mail.smtp.host")).isEqualTo("testserver");
        assertThat(smtp.getProperty("mail.smtp.auth")).isEqualTo("true");
        assertThat(smtp.getProperty("mail.smtp.user")).isEqualTo("testuser");
        assertThat(smtp.getProperty("mail.smtp.password")).isEqualTo("testpw");
    }

    @Test
    public void test_smtp2_session_configuration_is_present() {
        assertThat(sessionsConfig).containsKey("smtp2");
        final Session smtp2 = sessionsConfig.get("smtp2");
        assertThat(smtp2).isNotNull();
        assertThat(smtp2.getProperty("mail.smtp.host")).isEqualTo("testserver2");
        assertThat(smtp2.getProperty("mail.smtp.auth")).isEqualTo("true");
        assertThat(smtp2.getProperty("mail.smtp.user")).isEqualTo("testuser2");
        assertThat(smtp2.getProperty("mail.smtp.password")).isEqualTo("testpw2");
    }

    @Test
    public void test_imap_session_configuration_is_present() {
        assertThat(sessionsConfig).containsKey("imap");
        final Session imap = sessionsConfig.get("imap");
        assertThat(imap).isNotNull();
        assertThat(imap.getProperty("mail.imap.user")).isEqualTo("toto_user@ext.mpsa.com");
        assertThat(imap.getProperty("mail.imap.host")).isEqualTo("testserver3");
        assertThat(imap.getProperty("mail.imap.auth.login.disable")).isEqualTo(Boolean.FALSE.toString());
        assertThat(imap.getProperty("mail.imap.auth.plain.disable")).isEqualTo(Boolean.TRUE.toString());
    }

    @Test
    public void test_pop3_session_configuration_is_present() {
        assertThat(sessionsConfig).containsKey("pop3");
        final Session pop3 = sessionsConfig.get("pop3");
        assertThat(pop3.getProperty("mail.pop3.host")).isEqualTo("testserver4");
        assertThat(pop3).isNotNull();
    }
}