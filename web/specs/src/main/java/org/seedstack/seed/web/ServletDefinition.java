/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web;

import javax.servlet.Servlet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServletDefinition extends AbstractDefinition {
    private final Class<? extends Servlet> servletClass;
    private final List<String> mappings = new ArrayList<String>();
    private int loadOnStartup = -1;

    public ServletDefinition(String name, Class<? extends Servlet> servletClass) {
        super(name);
        this.servletClass = servletClass;
    }

    public Class<? extends Servlet> getServletClass() {
        return servletClass;
    }

    public int getLoadOnStartup() {
        return loadOnStartup;
    }

    public void setLoadOnStartup(int loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    public String[] getMappings() {
        return mappings.toArray(new String[mappings.size()]);
    }

    public void addMappings(String... mappings) {
        this.mappings.addAll(Arrays.asList(mappings));
    }
}
