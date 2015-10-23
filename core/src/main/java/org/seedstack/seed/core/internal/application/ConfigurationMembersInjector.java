/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.application;

import com.google.inject.MembersInjector;
import org.seedstack.seed.core.api.ErrorCode;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.spi.configuration.ConfigurationConverter;
import org.seedstack.seed.core.spi.configuration.ConfigurationIdentityConverter;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Performs field injection of configuration data.
 *
 * @author adrien.lauer@mpsa.com
 * @author yves.dautremay@mpsa.com
 */
class ConfigurationMembersInjector<T> implements MembersInjector<T> {

    private static final String NO_PROPERTY_FOUND_LOG_MESSAGE = "no property {} found, injecting default value";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationMembersInjector.class);

    private final Field field;

    private final Configuration configuration;

    private org.seedstack.seed.core.api.Configuration annotation;

    private static Map<Class<?>, Class<? extends ConfigurationConverter<?>>> converters = new HashMap<Class<?>, Class<? extends ConfigurationConverter<?>>>();
    static {
        converters.put(Integer.class, Converters.IntegerConverter.class);
        converters.put(Integer.TYPE, Converters.IntegerConverter.class);
        converters.put(Short.class, Converters.ShortConverter.class);
        converters.put(Short.TYPE, Converters.ShortConverter.class);
        converters.put(Boolean.class, Converters.BooleanConverter.class);
        converters.put(Boolean.TYPE, Converters.BooleanConverter.class);
        converters.put(Byte.class, Converters.ByteConverter.class);
        converters.put(Byte.TYPE, Converters.ByteConverter.class);
        converters.put(Long.class, Converters.LongConverter.class);
        converters.put(Long.TYPE, Converters.LongConverter.class);
        converters.put(Float.class, Converters.FloatConverter.class);
        converters.put(Float.TYPE, Converters.FloatConverter.class);
        converters.put(Double.class, Converters.DoubleConverter.class);
        converters.put(Double.TYPE, Converters.DoubleConverter.class);
        converters.put(Character.class, Converters.CharacterConverter.class);
        converters.put(Character.TYPE, Converters.CharacterConverter.class);
        converters.put(String.class, ConfigurationIdentityConverter.class);
    }

    ConfigurationMembersInjector(Field field, Configuration configuration, org.seedstack.seed.core.api.Configuration annotation) {
        this.field = field;
        this.configuration = configuration;
        this.annotation = annotation;
    }

    @Override
    public void injectMembers(T instance) {
        String configurationParameterName = annotation.value();
        Enum<? extends ErrorCode> enumerate = findEnum();
        // Pre verification
        if(enumerate == null){
            LOGGER.warn("Unable to find enum {}.{} in @Configuration annotation on field {}.{}", annotation.errorCodeClass().getSimpleName(), annotation.errorCodeName(), field.getDeclaringClass().getCanonicalName(), field.getName());
            enumerate = ApplicationErrorCode.CONFIGURATION_ERROR;
        }
        if (!configuration.containsKey(configurationParameterName) && annotation.mandatory() && annotation.defaultValue().length == 0) {
            throw SeedException.createNew((ErrorCode) enumerate).put("property", configurationParameterName).put("field", field.getName()).put("class", field.getDeclaringClass().getCanonicalName());
        }
        Class<?> type = field.getType();
        if (type.isArray()) {
            writeArrayField(instance);
        } else {
            writeSimpleField(instance);
        }
    }

    private Enum<? extends ErrorCode> findEnum(){
        Class<? extends Enum<? extends ErrorCode>> errorCodeClass = annotation.errorCodeClass();
        if (errorCodeClass.equals(org.seedstack.seed.core.api.Configuration.ConfigurationErrorCode.class)) {
            errorCodeClass = ApplicationErrorCode.class;
        }

        for (Enum<? extends ErrorCode> enumElement : errorCodeClass.getEnumConstants()) {
            if(enumElement.name().equals(annotation.errorCodeName())){
                return enumElement;
            }
        }

        return null;
    }

    private void writeSimpleField(T instance) {
        ConfigurationConverter<?> converter;
        converter = findConverter(instance, field.getType());
        String value = configuration.getString(annotation.value());

        if (value == null && annotation.defaultValue().length > 0) {
            value = annotation.defaultValue()[0];
        }

        if (value != null) {
            writeField(instance, converter.convert(value));
        } else {
            LOGGER.debug(NO_PROPERTY_FOUND_LOG_MESSAGE, annotation.value());
        }
    }

    @SuppressWarnings("unchecked")
    private void writeArrayField(T instance) {
        ConfigurationConverter<?> converter;
        Class<?> componentType = field.getType().getComponentType();
        converter = findConverter(instance, componentType);
        String[] values = configuration.getStringArray(annotation.value());

        if ((values == null || values.length == 0) && annotation.defaultValue().length > 0) {
            values = annotation.defaultValue();
        }

        if (values != null && values.length > 0) {
            if (componentType.isPrimitive()) {
                if (componentType == Short.TYPE) {
                    writeField(instance, convertToShortValues(values, (ConfigurationConverter<Short>) converter));
                }
                if (componentType == Integer.TYPE) {
                    writeField(instance, convertToIntegerValues(values, (ConfigurationConverter<Integer>) converter));
                }
                if (componentType == Boolean.TYPE) {
                    writeField(instance, convertToBooleanValues(values, (ConfigurationConverter<Boolean>) converter));
                }
                if (componentType == Byte.TYPE) {
                    writeField(instance, convertToByteValues(values, (ConfigurationConverter<Byte>) converter));
                }
                if (componentType == Long.TYPE) {
                    writeField(instance, convertToLongValues(values, (ConfigurationConverter<Long>) converter));
                }
                if (componentType == Float.TYPE) {
                    writeField(instance, convertToFloatValues(values, (ConfigurationConverter<Float>) converter));
                }
                if (componentType == Double.TYPE) {
                    writeField(instance, convertToDoubleValues(values, (ConfigurationConverter<Double>) converter));
                }
                if (componentType == Character.TYPE) {
                    writeField(instance, convertToCharacterValues(values, (ConfigurationConverter<Character>) converter));
                }
            } else {
                Object[] convertedValues;
                try {
                    convertedValues = (Object[])Array.newInstance(field.getType().getComponentType(), values.length);
                } catch(Exception e) {
                    throw SeedException.wrap(e, ApplicationErrorCode.UNABLE_TO_INSTANTIATE_CONFIGURATION_ARRAY);
                }

                for (int i = 0; i < values.length; i++) {
                    convertedValues[i] = converter.convert(values[i]);
                }
                writeField(instance, convertedValues);
            }
        } else {
            LOGGER.debug(NO_PROPERTY_FOUND_LOG_MESSAGE, annotation.value());
        }
    }

    private void writeField(T instance, Object convertedValues) {
        try {
            FieldUtils.writeField(field, instance, convertedValues, true);
        } catch (IllegalAccessException ex) {
            throw SeedException.wrap(ex, ApplicationErrorCode.FIELD_ILLEGAL_ACCESS).put("class", instance.getClass()).put("field", field.getName());
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ConfigurationConverter<?> findConverter(T instance, Class<?> type) {
        Class<? extends ConfigurationConverter<?>> converterClass = annotation.converter();
        try {
            if (converterClass.equals(ConfigurationIdentityConverter.class) && !type.equals(String.class)) {
                // default can't work
                if (Enum.class.isAssignableFrom(type)) {
                    return new Converters.EnumConverter(type);
                } else if (converters.containsKey(type)) {
                    return converters.get(type).newInstance();
                } else {
                    throw SeedException.createNew(ApplicationErrorCode.CONVERTER_NOT_COMPATIBLE).put("class", instance.getClass()).put("field", field.getName()).put("fieldType", type);
                }
            } else {
                return converterClass.newInstance();
            }
        } catch (InstantiationException ex) {
            throw SeedException.wrap(ex, ApplicationErrorCode.CONVERTER_INSTANTIATION).put("class", instance.getClass()).put("field", field.getName()).put("converterClass", converterClass);
        } catch (IllegalAccessException ex) {
            throw SeedException.wrap(ex, ApplicationErrorCode.CONVERTER_CONSTRUCTOR_ILLEGAL_ACCESS).put("class", instance.getClass()).put("field", field.getName()).put("converterClass", converterClass);
        }
    }

    private short[] convertToShortValues(String[] values, ConfigurationConverter<Short> converter) {
        short[] convertedValues = new short[values.length];
        for (int i = 0; i < values.length; i++) {
            convertedValues[i] = converter.convert(values[i]);
        }
        return convertedValues;
    }

    private int[] convertToIntegerValues(String[] values, ConfigurationConverter<Integer> converter) {
        int[] convertedValues = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            convertedValues[i] = converter.convert(values[i]);
        }
        return convertedValues;
    }

    private boolean[] convertToBooleanValues(String[] values, ConfigurationConverter<Boolean> converter) {
        boolean[] convertedValues = new boolean[values.length];
        for (int i = 0; i < values.length; i++) {
            convertedValues[i] = converter.convert(values[i]);
        }
        return convertedValues;
    }

    private byte[] convertToByteValues(String[] values, ConfigurationConverter<Byte> converter) {
        byte[] convertedValues = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            convertedValues[i] = converter.convert(values[i]);
        }
        return convertedValues;
    }

    private long[] convertToLongValues(String[] values, ConfigurationConverter<Long> converter) {
        long[] convertedValues = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            convertedValues[i] = converter.convert(values[i]);
        }
        return convertedValues;
    }

    private float[] convertToFloatValues(String[] values, ConfigurationConverter<Float> converter) {
        float[] convertedValues = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            convertedValues[i] = converter.convert(values[i]);
        }
        return convertedValues;
    }

    private double[] convertToDoubleValues(String[] values, ConfigurationConverter<Double> converter) {
        double[] convertedValues = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            convertedValues[i] = converter.convert(values[i]);
        }
        return convertedValues;
    }

    private char[] convertToCharacterValues(String[] values, ConfigurationConverter<Character> converter) {
        char[] convertedValues = new char[values.length];
        for (int i = 0; i < values.length; i++) {
            convertedValues[i] = converter.convert(values[i]);
        }
        return convertedValues;
    }
}
