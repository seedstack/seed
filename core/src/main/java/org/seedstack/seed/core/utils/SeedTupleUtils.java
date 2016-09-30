/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

import com.google.common.collect.Lists;
import org.javatuples.Decade;
import org.javatuples.Ennead;
import org.javatuples.Octet;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Septet;
import org.javatuples.Sextet;
import org.javatuples.Triplet;
import org.javatuples.Tuple;
import org.javatuples.Unit;
import org.seedstack.shed.exception.SeedException;

import java.util.Collection;
import java.util.List;

/**
 * Tuple utilities.
 *
 * @author redouane.loulou@ext.mpsa.com
 */
public final class SeedTupleUtils {
    private SeedTupleUtils() {
    }

    /**
     * Create a Tuple from an object array.
     *
     * @param objects the array of objects.
     * @param <T>     the tuple type.
     * @return the tuple.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Tuple> T createTupleFromArray(Object[] objects) {
        return (T) createTupleFromList((List) Lists.newArrayList(objects));
    }

    /**
     * Create a Tuple from an object list.
     *
     * @param objects the list of objects.
     * @param <T>     the tuple type.
     * @return the tuple.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Tuple> T createTupleFromList(List<Object> objects) {
        Class<? extends Tuple> tupleClass = null;

        switch (objects.size()) {
            case 1:
                tupleClass = Unit.class;
                break;
            case 2:
                tupleClass = Pair.class;
                break;
            case 3:
                tupleClass = Triplet.class;
                break;
            case 4:
                tupleClass = Quartet.class;
                break;
            case 5:
                tupleClass = Quintet.class;
                break;
            case 6:
                tupleClass = Sextet.class;
                break;
            case 7:
                tupleClass = Septet.class;
                break;
            case 8:
                tupleClass = Octet.class;
                break;
            case 9:
                tupleClass = Ennead.class;
                break;
            case 10:
                tupleClass = Decade.class;
                break;
            default:
                break;
        }

        if (tupleClass == null) {
            throw SeedException.createNew(CoreUtilsErrorCode.ERROR_BUILDING_TUPLE);
        }

        try {
            return (T) tupleClass.getMethod("fromCollection", Collection.class).invoke(null, objects);
        } catch (Exception e) {
            throw SeedException.wrap(e, CoreUtilsErrorCode.ERROR_BUILDING_TUPLE);
        }
    }
}
