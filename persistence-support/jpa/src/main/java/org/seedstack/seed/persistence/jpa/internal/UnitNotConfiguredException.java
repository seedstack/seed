/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 19 mars 2015
 */
package org.seedstack.seed.persistence.jpa.internal;

class UnitNotConfiguredException extends Exception {

    private static final long serialVersionUID = 6525564562654268478L;

    UnitNotConfiguredException() {
        super();
    }

    UnitNotConfiguredException(String message, Throwable cause) {
        super(message, cause);
    }

    UnitNotConfiguredException(String message) {
        super(message);
    }

    UnitNotConfiguredException(Throwable cause) {
        super(cause);
    }

}
