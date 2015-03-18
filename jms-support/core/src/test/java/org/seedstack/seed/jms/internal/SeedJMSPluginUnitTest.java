/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.jms.internal;

import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.core.internal.jndi.JndiPlugin;
import org.seedstack.seed.transaction.internal.TransactionPlugin;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.context.InitContext;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.kametic.specifications.Specification;
import org.mockito.Mockito;

import javax.naming.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SeedJMSPluginUnitTest {

    JmsPlugin underTest;
    Configuration conf = new PropertiesConfiguration();

    @Before
    public void setUp() throws Exception {
        underTest = new JmsPlugin();
        conf.addProperty("org.seedstack.seed.jms.connectionFactory.default.class", "org.apache.activemq.ActiveMQConnectionFactory");
        conf.addProperty("org.seedstack.seed.jms.connectionFactory.default.url", "vm://localhost?broker.persistent=false");

    }

    @Test
    public void testName() {

        assertThat(underTest.name()).isEqualTo("seed-jms-plugin");
    }

    @Test
    public void testInit() throws ConfigurationException {
        underTest.init(buildCoherentInitContext(conf));
    }

    @Test
    public void testDependencyInjectionDef() throws ConfigurationException {
        underTest.init(buildCoherentInitContext(conf));
        Object actual = underTest.nativeUnitModule();
        assertThat(actual).isInstanceOf(JmsModule.class);
    }

    @Test
    public void testRequiredPlugins() {
        Collection<Class<? extends Plugin>> actual = underTest.requiredPlugins();
        assertThat(actual).isNotEmpty();
        assertThat(actual.size()).isEqualTo(3);
        assertThat(actual.contains(ApplicationPlugin.class)).isEqualTo(true);
        assertThat(actual.contains(TransactionPlugin.class)).isEqualTo(true);
    }

    @SuppressWarnings("unchecked")
    private InitContext buildCoherentInitContext(Configuration conf) throws ConfigurationException {
        InitContext initContext = mock(InitContext.class);

        ApplicationPlugin confPlugin = mock(ApplicationPlugin.class);
        Application application = mock(Application.class);
        when(application.getConfiguration()).thenReturn(conf);
        when(application.getId()).thenReturn("test-app-id");
        when(confPlugin.getApplication()).thenReturn(application);

        TransactionPlugin txplugin = mock(TransactionPlugin.class);
        JndiPlugin jndiplugin = mock(JndiPlugin.class);
        when(jndiplugin.getJndiContexts()).thenReturn(new HashMap<String, Context>());

        Collection pluginsRequired = new ArrayList<Plugin>();
        pluginsRequired.add(confPlugin);
        pluginsRequired.add(txplugin);
        pluginsRequired.add(jndiplugin);

        HashMap<Specification, Collection<Class<?>>> map = mock(HashMap.class);

        when(initContext.pluginsRequired()).thenReturn(pluginsRequired);
        when(initContext.scannedTypesBySpecification()).thenReturn(map);

        when(map.get(Mockito.any(Specification.class))).thenReturn(Collections.EMPTY_LIST);
        return initContext;

    }
}
