/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.Servlet;

/**
 * This class holds the full definition of a Servlet servlet. It can be returned as a collection element from
 * {@link WebProvider#servlets()}
 * to define the servlets that must be registered by Seed.
 */
public class ServletDefinition extends AbstractDefinition {
    private final Class<? extends Servlet> servletClass;
    private final List<String> mappings = new ArrayList<>();
    private int loadOnStartup = -1;

    /**
     * Creates a servlet definition with the specified name and class.
     *
     * @param name         the servlet name.
     * @param servletClass the servlet class.
     */
    public ServletDefinition(String name, Class<? extends Servlet> servletClass) {
        super(name);
        this.servletClass = servletClass;
    }

    /**
     * @return the Servlet class.
     */
    public Class<? extends Servlet> getServletClass() {
        return servletClass;
    }

    /**
     * @return true if the Servlet should be loaded on startup, false otherwise.
     */
    public int getLoadOnStartup() {
        return loadOnStartup;
    }

    /**
     * Sets if the Servlet should be loaded on startup.
     *
     * @param loadOnStartup true if the Servlet should be loaded on startup, false otherwise.
     */
    public void setLoadOnStartup(int loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    /**
     * @return the servlet mappings (URL patterns).
     */
    public String[] getMappings() {
        return mappings.toArray(new String[mappings.size()]);
    }

    /**
     * Add mappings to this Servlet definition.
     *
     * @param mappings the servlet mappings (URL patterns).
     */
    public void addMappings(String... mappings) {
        this.mappings.addAll(Arrays.asList(mappings));
    }
}
