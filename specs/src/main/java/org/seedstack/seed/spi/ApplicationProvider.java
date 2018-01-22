/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.spi;

import io.nuun.kernel.api.annotations.Facet;
import org.seedstack.seed.Application;

/**
 * This facet can be requested as dependency by plugins to access the application object. This is most useful to
 * access the configuration.
 */
@Facet
public interface ApplicationProvider {
    /**
     * @return the application object.
     */
    Application getApplication();
}
