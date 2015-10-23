/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import org.seedstack.seed.it.api.ITBind;

@ITBind
public class InstrumentedWithExceptionMetered {

    @ExceptionMetered(name = "exception_metered_exceptionCounter")
    String explodeWithPublicScope(boolean explode) {
        if (explode) {
            throw new RuntimeException("Boom!");
        } else {
            return "calm";
        }
    }

    @ExceptionMetered
    String explodeForUnnamedMetric() {
        throw new RuntimeException("Boom!");
    }

    @ExceptionMetered(name = "exception_metered_n")
    String explodeForMetricWithName() {
        throw new RuntimeException("Boom!");
    }

    @ExceptionMetered(name = "exception_metered_absoluteName", absolute = true)
    String explodeForMetricWithAbsoluteName() {
        throw new RuntimeException("Boom!");
    }

    @ExceptionMetered
    String explodeWithDefaultScope() {
        throw new RuntimeException("Boom!");
    }

    @ExceptionMetered
    protected String explodeWithProtectedScope() {
        throw new RuntimeException("Boom!");
    }

    @ExceptionMetered(name = "exception_metered_failures", cause = MyException.class)
    public void errorProneMethod(RuntimeException e) {
        throw e;
    }

    @ExceptionMetered(name = "exception_metered_things",
            cause = ArrayIndexOutOfBoundsException.class)
    public Object causeAnOutOfBoundsException() {
        final Object[] arr = {};
        return arr[1];
    }

    @Timed
    @ExceptionMetered
    public void timedAndException(RuntimeException e) {
        if (e != null) {
            throw e;
        }
    }

    @Metered
    @ExceptionMetered
    public void meteredAndException(RuntimeException e) {
        if (e != null) {
            throw e;
        }
    }
}
