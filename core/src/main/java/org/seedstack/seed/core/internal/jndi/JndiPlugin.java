/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.jndi;

import com.google.common.collect.Lists;
import org.seedstack.seed.SeedException;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.internal.CorePlugin;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Plugin that retrieve configured JNDI contexts.
 *
 * @author adrien.lauer@mpsa.com
 */
public class JndiPlugin extends AbstractPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(JndiPlugin.class);

    private final Map<String, Context> additionalJndiContexts = new HashMap<String, Context>();
    private Context defaultJndiContext;

    @Override
    public String name() {
        return "jndi";
    }

    @Override
    public InitState init(InitContext initContext) {
        Configuration configuration = initContext.dependency(ConfigurationProvider.class).getConfiguration().subset(CorePlugin.CORE_PLUGIN_PREFIX);

        // Default JNDI context
        try {
            this.defaultJndiContext = new InitialContext();
            LOGGER.debug("Default JNDI context has been configured");
        } catch (NamingException e) {
            throw SeedException.wrap(e, JndiErrorCode.UNABLE_TO_CONFIGURE_DEFAULT_JNDI_CONTEXT);
        }

        // Additional JNDI contexts
        String[] jndiContextNames = configuration.getStringArray("additional-jndi-contexts");
        if (jndiContextNames != null) {
            for (String jndiContextName : jndiContextNames) {
                String propertiesResource = configuration.getString("additional-jndi-context." + jndiContextName);
                Properties properties = new Properties();
                InputStream propertiesResourceStream = this.getClass().getResourceAsStream(propertiesResource);

                if (propertiesResourceStream != null) {
                    try {
                        properties.load(propertiesResourceStream);
                        this.additionalJndiContexts.put(jndiContextName, new InitialContext(properties));
                        LOGGER.debug("JNDI context " + jndiContextName + " has been configured from " + propertiesResource);
                    } catch (Exception e) {
                        throw SeedException.wrap(e, JndiErrorCode.UNABLE_TO_CONFIGURE_ADDITIONAL_JNDI_CONTEXT).put("context", jndiContextName);
                    }

                    try {
                        propertiesResourceStream.close();
                    } catch (IOException e) {
                        LOGGER.warn("Unable to close JNDI properties resource " + propertiesResource, e);
                    }
                } else {
                    throw SeedException.createNew(JndiErrorCode.MISSING_JNDI_PROPERTIES).put("context", jndiContextName).put("property", "org.seedstack.seed.core.additional-jndi-context." + jndiContextName + " property");
                }
            }
        }
        return InitState.INITIALIZED;
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(ConfigurationProvider.class);
    }

    @Override
    public Object nativeUnitModule() {
        return new JndiModule(this.defaultJndiContext, this.additionalJndiContexts);
    }

    /**
     * Retrieve all configured JNDI contexts.
     *
     * @return the map of all configured JNDI contexts.
     */
    public Map<String, Context> getJndiContexts() {
        Map<String, Context> jndiContexts = new HashMap<String, Context>();
        jndiContexts.putAll(additionalJndiContexts);
        jndiContexts.put("default", defaultJndiContext);
        return jndiContexts;
    }
}
