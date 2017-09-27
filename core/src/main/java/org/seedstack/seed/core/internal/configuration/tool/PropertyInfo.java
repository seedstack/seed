/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.configuration.tool;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

class PropertyInfo {
    private String name;
    private String type;
    private String shortDescription;
    private String longDescription;
    private boolean singleValue;
    private boolean mandatory;
    private Object defaultValue;
    private Map<String, PropertyInfo> innerPropertyInfo = new TreeMap<>();

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    String getType() {
        return type;
    }

    void setType(String type) {
        this.type = type;
    }

    String getShortDescription() {
        return shortDescription;
    }

    void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    String getLongDescription() {
        return longDescription;
    }

    void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    boolean isSingleValue() {
        return singleValue;
    }

    void setSingleValue(boolean singleValue) {
        this.singleValue = singleValue;
    }

    boolean isMandatory() {
        return mandatory;
    }

    void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    Object getDefaultValue() {
        return defaultValue;
    }

    void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    Map<String, PropertyInfo> getInnerPropertyInfo() {
        return Collections.unmodifiableMap(innerPropertyInfo);
    }

    void addInnerPropertyInfo(Map<String, PropertyInfo> innerPropertyInfo) {
        this.innerPropertyInfo.putAll(innerPropertyInfo);
    }
}
