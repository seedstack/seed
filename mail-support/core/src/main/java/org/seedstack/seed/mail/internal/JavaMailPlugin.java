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

import com.google.common.collect.Maps;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.mail.spi.SessionConfigurer;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;

import javax.mail.Session;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * This plugin is responsible for providing injectable JavaMail @see (javax.mail.Session) sessions based on the different
 * protocol providers configured in the configuration files a number of instances will be created.
 *
 * @author aymen.benhmida@ext.mpsa.com
 */
public class JavaMailPlugin extends AbstractPlugin {
    public static final String CONFIGURATION_PREFIX = "org.seedstack.seed.mail";

    private final Map<String, Session> sessions = Maps.newHashMap();

    @Override
    public String name() {
        return "seed-mail-plugin";
    }

    @Override
    public InitState init(InitContext initContext) {
        Configuration mailSessionsConfiguration = null;

        for (Plugin deployedPlugin : initContext.pluginsRequired()) {
            mailSessionsConfiguration = getPluginConfiguration(deployedPlugin);
        }

        failIfConfigurationIsAbsent(mailSessionsConfiguration);

        SessionConfigurer configurer = new PropertyFileSessionConfigurer(mailSessionsConfiguration);
        sessions.putAll(configurer.doConfigure());

        return InitState.INITIALIZED;
    }

    void failIfConfigurationIsAbsent(Configuration mailSessionsConfiguration) {
        if (mailSessionsConfiguration == null) {
            throw new PluginException("Unable to find mail plugin configuration");
        }
    }

    Configuration getPluginConfiguration(Plugin deployedPlugin) {
        Configuration mailSessionsConfiguration = null;

        if (deployedPlugin instanceof ApplicationPlugin) {
            ApplicationPlugin applicationPlugin = (ApplicationPlugin) deployedPlugin;
            mailSessionsConfiguration = applicationPlugin.getApplication().getConfiguration().subset(JavaMailPlugin.CONFIGURATION_PREFIX);
        }

        return mailSessionsConfiguration;
    }

    @Override
    public Object nativeUnitModule() {
        return new JavaMailModule(sessions);
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        ArrayList<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        return plugins;
    }
}
