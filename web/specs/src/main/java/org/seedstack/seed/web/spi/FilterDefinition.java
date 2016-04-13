/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.spi;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class FilterDefinition extends AbstractDefinition {
    private final Class<? extends Filter> filterClass;
    private int priority = 0;
    private List<Mapping> mappings = new ArrayList<Mapping>();
    private List<Mapping> servletMappings = new ArrayList<Mapping>();

    public FilterDefinition(String name, Class<? extends Filter> filterClass) {
        super(name);
        this.filterClass = filterClass;
    }

    public Class<? extends Filter> getFilterClass() {
        return filterClass;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Mapping[] getMappings() {
        return mappings.toArray(new Mapping[mappings.size()]);
    }

    public void addMappings(Mapping... mappings) {
        this.mappings.addAll(Arrays.asList(mappings));
    }

    public Mapping[] getServletMappings() {
        return servletMappings.toArray(new Mapping[servletMappings.size()]);
    }

    public void addServletMappings(Mapping... servletMappings) {
        this.servletMappings.addAll(Arrays.asList(servletMappings));
    }

    public static class Mapping {
        private final EnumSet<DispatcherType> dispatcherTypes;
        private final boolean isMatchAfter;
        private final String[] values;

        public Mapping(String... values) {
            this.dispatcherTypes = EnumSet.allOf(DispatcherType.class);
            this.isMatchAfter = false;
            this.values = values;
        }

        public Mapping(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... values) {
            this.dispatcherTypes = dispatcherTypes;
            this.isMatchAfter = isMatchAfter;
            this.values = values;
        }

        public EnumSet<DispatcherType> getDispatcherTypes() {
            return dispatcherTypes;
        }

        public boolean isMatchAfter() {
            return isMatchAfter;
        }

        public String[] getValues() {
            return values;
        }
    }
}
