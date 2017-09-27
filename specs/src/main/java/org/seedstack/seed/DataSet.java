/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks {@link DataImporter}s and {@link DataExporter}s to specify the
 * data set they are handling. Data sets have two attributes:
 * <ul>
 * <li>The group attribute identifies a functional grouping of the data</li>
 * <li>The name attribute identifies a particular homogeneous collection of objects inside the group.</li>
 * </ul>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DataSet {
    /**
     * @return the name of the group.
     */
    String group();

    /**
     * @return the name of the data set.
     */
    String name();
}
