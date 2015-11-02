/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

import jodd.bean.BeanUtil;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.SeedException;

import java.util.Properties;

/**
 * Class with various utility methods for creating objects and setting their properties.
 *
 * @author adrien.lauer@mpsa.com
 */
public final class SeedBeanUtils {

    private SeedBeanUtils() {
    }

    /**
     * Set properties derived from configuration on a bean.
     *
     * <ul>
     * <li>[prefix].property.* gives the properties to set.</li>
     * </ul>
     *
     * @param bean          the bean to set properties on.
     * @param configuration the configuration to derive properties from.
     * @param prefix        the property prefix.
     */
    public static void setPropertiesFromConfiguration(Object bean, Configuration configuration, String prefix) {
        Properties properties = SeedConfigurationUtils.buildPropertiesFromConfiguration(configuration, prefix);
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            try {
                if (!BeanUtil.hasProperty(bean, key)) {
                    throw SeedException.createNew(CoreUtilsErrorCode.PROPERTY_NOT_FOUND).put("property", key).put("class", bean.getClass().getCanonicalName());
                }

                BeanUtil.setProperty(bean, key, value);
            } catch (Exception e) {
                throw SeedException.wrap(e, CoreUtilsErrorCode.UNABLE_TO_SET_PROPERTY).put("property", key).put("class", bean.getClass().getCanonicalName()).put("value", value);
            }
        }
    }

    /**
     * Create a bean derived from configuration.
     *
     * <ul>
     * <li>[prefix].class gives the class of the bean.</li>
     * <li>[prefix].property.* gives the properties to set.</li>
     * </ul>
     *
     * @param configuration the configuration to derive the bean from.
     * @param prefix        the property prefix.
     * @return the newly created bean with appropriate properties already set.
     */
    public static Object createFromConfiguration(Configuration configuration, String prefix) {
        final String classname = configuration.getString(prefix + ".class");

        Class<?> beanClass;
        try {
            beanClass = Class.forName(classname);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to load class " + classname, e);
        }

        Object object;
        try {
            object = beanClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate object from class " + classname, e);
        }

        setPropertiesFromConfiguration(object, configuration, prefix + ".property");

        return object;
    }
}
