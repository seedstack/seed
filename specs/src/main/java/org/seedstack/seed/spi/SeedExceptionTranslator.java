/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.spi;

import org.seedstack.shed.exception.BaseException;

/**
 * This interface can be implemented to provide an exception translator, capable of translating specific exceptions to
 * a {@link BaseException}.
 * Implementations must be declared as a {@link java.util.ServiceLoader} service in META-INF/services to be detected.
 */
public interface SeedExceptionTranslator {
    /**
     * Check if the specified exception can be translated.
     *
     * @param e The candidate exception.
     * @return true if this translator can translate it, false otherwise.
     */
    boolean canTranslate(Exception e);

    /**
     * Translate the specified exception. It is strongly recommended to put the original exception as the cause of the
     * translated one.
     *
     * @param e The exception to translate.
     * @return the translated exception.
     */
    BaseException translate(Exception e);
}
