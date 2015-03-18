/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.spring.internal;

import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.FactoryBean;

class SeedConfigurationFactoryBean implements FactoryBean<Object> {
    static Configuration configuration;
    private String key;
    private String defaultValue;
    private boolean mandatory = true;

    @Override
    public Object getObject() throws Exception {
        if (key == null) {
            throw new IllegalArgumentException("Property key is required for SeedConfigurationFactoryBean");
        } else {
            String value = configuration.getString(key);
            if (value == null) {
                if (defaultValue == null && mandatory) {
                    throw new IllegalArgumentException("Configuration value " + key + " is mandatory and has no value nor default value");
                }

                return defaultValue;
            } else {
                return value;
            }
        }
    }

    @Override
    public Class<?> getObjectType() {
        return String.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }
}
