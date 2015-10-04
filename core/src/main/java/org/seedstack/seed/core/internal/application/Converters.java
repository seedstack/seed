/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.application;

import org.seedstack.seed.core.spi.configuration.ConfigurationConverter;

/**
 * This class is a container for built-in configuration converters.
 *
 * @author yves.dautremay@mpsa.com
 */
final class Converters {
    private Converters() {
        // hide default constructor
    }

    static class ShortConverter implements ConfigurationConverter<Short> {

        @Override
        public Short convert(String value) {
            return Short.valueOf(value);
        }

    }

    static class LongConverter implements ConfigurationConverter<Long> {

        @Override
        public Long convert(String value) {
            return Long.valueOf(value);
        }

    }

    static class IntegerConverter implements ConfigurationConverter<Integer> {

        @Override
        public Integer convert(String value) {
            return Integer.valueOf(value);
        }

    }

    static class FloatConverter implements ConfigurationConverter<Float> {

        @Override
        public Float convert(String value) {
            return Float.valueOf(value);
        }

    }

    static class EnumConverter<E extends Enum<E>> implements ConfigurationConverter<E> {
        private final Class<E> enumClass;

        EnumConverter(Class<E> enumClass) {
            this.enumClass = enumClass;
        }

        @Override
        public E convert(String value) {
            return Enum.valueOf(enumClass, value);
        }
    }

    static class DoubleConverter implements ConfigurationConverter<Double> {

        @Override
        public Double convert(String value) {
            return Double.valueOf(value);
        }

    }

    static class CharacterConverter implements ConfigurationConverter<Character> {

        @Override
        public Character convert(String value) {
            return value.charAt(0);
        }

    }

    static class ByteConverter implements ConfigurationConverter<Byte> {

        @Override
        public Byte convert(String value) {
            return Byte.valueOf(value);
        }

    }

    static class BooleanConverter implements ConfigurationConverter<Boolean> {

        @Override
        public Boolean convert(String value) {
            return Boolean.valueOf(value);
        }

    }
}
