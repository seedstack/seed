/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.spi.data;


import java.util.HashMap;
import java.util.Map;

/**
 * This class defines the default obfuscation. Data is nullified.
 */
public class NullifyObfuscationHandler implements DataObfuscationHandler<Object> {

    private static Map<Class<?>, Object> nulls = new HashMap<>();

    @Override
    public Object obfuscate(Object data) {
        if (data == null) {
            return null;
        }
        return nulls.get(data.getClass());
    }

    static {
        nulls.put(Integer.class, 0);
        nulls.put(Integer.TYPE, 0);
        nulls.put(Short.class, new Short((short) 0));
        nulls.put(Short.TYPE, (short) 0);
        nulls.put(Boolean.class, Boolean.FALSE);
        nulls.put(Boolean.TYPE, false);
        nulls.put(Byte.class, new Byte((byte) 0));
        nulls.put(Byte.TYPE, (byte) 0);
        nulls.put(Long.class, new Long(0L));
        nulls.put(Long.TYPE, 0L);
        nulls.put(Float.class, new Float(0d));
        nulls.put(Float.TYPE, 0f);
        nulls.put(Double.class, new Double(0d));
        nulls.put(Double.TYPE, 0d);
        nulls.put(Character.class, new Character((char) 0));
        nulls.put(Character.TYPE, (char) 0);
        nulls.put(String.class, "");
    }

}
