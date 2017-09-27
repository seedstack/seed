/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.jndi;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.seedstack.seed.JndiConfig;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.shed.ClassLoaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin that retrieve configured JNDI contexts.
 */
public class JndiPlugin extends AbstractSeedPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(JndiPlugin.class);

    private final Map<String, Context> additionalJndiContexts = new HashMap<>();
    private Context defaultJndiContext;

    @Override
    public String name() {
        return "jndi";
    }

    @Override
    public InitState initialize(InitContext initContext) {
        JndiConfig jndiConfig = getConfiguration(JndiConfig.class);

        // Default JNDI context
        try {
            defaultJndiContext = new InitialContext();
            LOGGER.debug("Default JNDI context has been configured");
        } catch (NamingException e) {
            throw SeedException.wrap(e, JndiErrorCode.UNABLE_TO_CONFIGURE_DEFAULT_JNDI_CONTEXT);
        }

        // Additional JNDI contexts
        for (Map.Entry<String, String> entry : jndiConfig.getAdditionalContexts().entrySet()) {
            Properties contextProperties = new Properties();
            String contextPropertiesPath = entry.getValue();
            InputStream propertiesResourceStream = ClassLoaders.findMostCompleteClassLoader(
                    JndiPlugin.class).getResourceAsStream(contextPropertiesPath);

            String contextName = entry.getKey();
            if (propertiesResourceStream != null) {
                try {
                    contextProperties.load(propertiesResourceStream);
                    additionalJndiContexts.put(contextName, new InitialContext(contextProperties));
                    LOGGER.debug("JNDI context " + contextName + " has been configured from " + contextPropertiesPath);
                } catch (IOException | NamingException e) {
                    throw SeedException.wrap(e, JndiErrorCode.UNABLE_TO_CONFIGURE_ADDITIONAL_JNDI_CONTEXT).put(
                            "context", contextName);
                }

                try {
                    propertiesResourceStream.close();
                } catch (IOException e) {
                    LOGGER.warn("Unable to close JNDI properties resource " + contextPropertiesPath, e);
                }
            } else {
                throw SeedException.createNew(JndiErrorCode.MISSING_JNDI_PROPERTIES).put("context", contextName)
                        .put("property", "jndi." + contextName);
            }
        }

        return InitState.INITIALIZED;
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
        Map<String, Context> jndiContexts = new HashMap<>();
        jndiContexts.putAll(additionalJndiContexts);
        jndiContexts.put("default", defaultJndiContext);
        return jndiContexts;
    }
}
