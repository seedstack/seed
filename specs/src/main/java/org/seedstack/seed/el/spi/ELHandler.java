/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.el.spi;

import java.lang.annotation.Annotation;

/**
 * Implementing this interface allows to define an EL handler which will receive the results of the corresponding
 * EL expression evaluation.
 *
 * @param <T> the annotation from which this handler will receive evaluations.
 */
public interface ELHandler<T extends Annotation> {
    /**
     * This method is called with the result of the EL evaluation.
     *
     * @param value the result.
     */
    void handle(Object value);
}
