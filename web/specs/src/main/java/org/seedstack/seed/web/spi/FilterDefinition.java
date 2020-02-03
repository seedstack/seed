/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;

/**
 * This class holds the full definition of a Servlet filter. It can be returned as a collection element from
 * {@link WebProvider#filters()}
 * to define the filters that must be registered by Seed.
 */
public class FilterDefinition extends AbstractDefinition {
    private final Class<? extends Filter> filterClass;
    private int priority = SeedFilterPriority.NORMAL;
    private List<Mapping> mappings = new ArrayList<>();
    private List<Mapping> servletMappings = new ArrayList<>();

    /**
     * Creates a filter definition with the specified name and class.
     *
     * @param name        the filter name.
     * @param filterClass the filter class.
     */
    public FilterDefinition(String name, Class<? extends Filter> filterClass) {
        super(name);
        this.filterClass = filterClass;
    }

    /**
     * @return the Servlet filter class.
     */
    public Class<? extends Filter> getFilterClass() {
        return filterClass;
    }

    /**
     * @return the registration priority of this filter ({@link SeedFilterPriority#NORMAL} by default).
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the registration priority of this filter. Filters are registered in the order of increasing priority.
     *
     * @param priority the absolute priority of this filter.
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * @return the filter mappings.
     */
    public Mapping[] getMappings() {
        return mappings.toArray(new Mapping[mappings.size()]);
    }

    /**
     * Adds a mapping to this filter definition.
     *
     * @param mappings the mapping.
     * @see javax.servlet.FilterRegistration#addMappingForUrlPatterns(EnumSet, boolean, String...)
     */
    public void addMappings(Mapping... mappings) {
        this.mappings.addAll(Arrays.asList(mappings));
    }

    /**
     * @return the filter servlet mappings.
     */
    public Mapping[] getServletMappings() {
        return servletMappings.toArray(new Mapping[servletMappings.size()]);
    }

    /**
     * Adds a servlet mapping to this filter definition.
     *
     * @param servletMappings the servlet mapping.
     * @see javax.servlet.FilterRegistration#addMappingForServletNames(EnumSet, boolean, String...)
     */
    public void addServletMappings(Mapping... servletMappings) {
        this.servletMappings.addAll(Arrays.asList(servletMappings));
    }

    /**
     * This class holds the definition of a filter mapping.
     */
    public static class Mapping {
        private final EnumSet<DispatcherType> dispatcherTypes;
        private final boolean isMatchAfter;
        private final String[] values;

        /**
         * Creates a mapping for all {@link DispatcherType}s, with the isMatchAfter attribute set to false and with the
         * specified values.
         *
         * @param values the values of this mapping (either URL patterns or Servlet names).
         * @see javax.servlet.FilterRegistration#addMappingForServletNames(EnumSet, boolean, String...)
         * @see javax.servlet.FilterRegistration#addMappingForUrlPatterns(EnumSet, boolean, String...)
         */
        public Mapping(String... values) {
            this.dispatcherTypes = EnumSet.allOf(DispatcherType.class);
            this.isMatchAfter = false;
            this.values = values;
        }

        /**
         * Creates a mapping for the specified {@link DispatcherType}s, isMatchAfter and values.
         *
         * @param dispatcherTypes the dispatcher types for this mapping.
         * @param isMatchAfter    the isMatchAfter attribute.
         * @param values          the values of this mapping (either URL patterns or Servlet names).
         * @see javax.servlet.FilterRegistration#addMappingForServletNames(EnumSet, boolean, String...)
         * @see javax.servlet.FilterRegistration#addMappingForUrlPatterns(EnumSet, boolean, String...)
         */
        public Mapping(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... values) {
            this.dispatcherTypes = dispatcherTypes;
            this.isMatchAfter = isMatchAfter;
            this.values = values;
        }

        /**
         * @return the dispatcher types.
         */
        public EnumSet<DispatcherType> getDispatcherTypes() {
            return dispatcherTypes;
        }

        /**
         * @return the value of the isMatchAfter attribute
         */
        public boolean isMatchAfter() {
            return isMatchAfter;
        }

        /**
         * @return the values of this mapping.
         */
        public String[] getValues() {
            return values.clone();
        }
    }
}
