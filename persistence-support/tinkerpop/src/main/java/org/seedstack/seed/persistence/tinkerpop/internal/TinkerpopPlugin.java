/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.tinkerpop.internal;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerModule;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.persistence.tinkerpop.api.GraphExceptionHandler;
import org.seedstack.seed.transaction.internal.TransactionPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This plugin provides graph-oriented persistence support using Tinkerpop as an abstraction.
 *
 * @author adrien.lauer@mpsa.com
 */
public class TinkerpopPlugin extends AbstractPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(TinkerpopPlugin.class);

    public static final String TINKERPOP_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.seed.persistence.tinkerpop";

    private final Map<String, Graph> graphs = new HashMap<String, Graph>();
    private final Map<String, Class<? extends GraphExceptionHandler>> graphExceptionHandlerClasses = new HashMap<String, Class<? extends GraphExceptionHandler>>();
    private final FramedGraphFactory framedGraphFactory = new FramedGraphFactory(new GremlinGroovyModule(), new JavaHandlerModule());

    @Override
    public String name() {
        return "seed-persistence-tinkerpop-plugin";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InitState init(InitContext initContext) {
        Configuration tinkerpopConfiguration = null;
        TransactionPlugin transactionPlugin = null;
        for (Plugin plugin : initContext.pluginsRequired()) {
            if (plugin instanceof ApplicationPlugin) {
                tinkerpopConfiguration = ((ApplicationPlugin) plugin).getApplication().getConfiguration().subset(TinkerpopPlugin.TINKERPOP_PLUGIN_CONFIGURATION_PREFIX);
            } else if (plugin instanceof TransactionPlugin) {
                transactionPlugin = ((TransactionPlugin) plugin);
            }
        }

        if (tinkerpopConfiguration == null) {
            throw new PluginException("Unable to find application plugin");
        }
        if (transactionPlugin == null) {
            throw new PluginException("Unable to find transaction plugin");
        }

        String[] graphNames = tinkerpopConfiguration.getStringArray("graphs");
        if (graphNames != null) {
            for (String graphName : graphNames) {
                Configuration graphConfiguration = tinkerpopConfiguration.subset("graph." + graphName);

                String implementation = graphConfiguration.getString("implementation");
                if (implementation != null && !implementation.isEmpty()) {
                    try {
                        this.graphs.put(graphName, (Graph) Class.forName(implementation).newInstance());
                    } catch (Exception e) {
                        throw new PluginException("Unable to load class " + implementation, e);
                    }
                } else {
                    throw new IllegalArgumentException("The implementation is not specified for graph " + graphName);
                }

                String exceptionHandler = graphConfiguration.getString("exception-handler");
                if (exceptionHandler != null && !exceptionHandler.isEmpty()) {
                    try {
                        graphExceptionHandlerClasses.put(graphName, (Class<? extends GraphExceptionHandler>) Class.forName(exceptionHandler));
                    } catch (Exception e) {
                        throw new PluginException("Unable to load class " + exceptionHandler, e);
                    }
                }
            }

            transactionPlugin.registerTransactionHandler(GraphTransactionHandler.class);
        } else {
            LOGGER.info("No Tinkerpop graph configured, Tinkerpop support disabled");
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        plugins.add(TransactionPlugin.class);
        return plugins;
    }

    @Override
    public Object nativeUnitModule() {
        if (this.graphs.isEmpty()) {
            return null;
        }

        return new TinkerpopModule(this.graphs, this.graphExceptionHandlerClasses, this.framedGraphFactory);
    }
}
