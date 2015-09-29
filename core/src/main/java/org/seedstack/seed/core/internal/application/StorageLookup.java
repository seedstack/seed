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

import org.apache.commons.lang.text.StrLookup;
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.spi.configuration.ConfigurationLookup;

/**
 * This class creates a Seed local storage subdirectory and resolves to it.
 *
 * @author adrien.lauer@mpsa.com
 */
@ConfigurationLookup("storage")
public class StorageLookup extends StrLookup {
    private final Application application;

    /**
     * Creates the lookup.
     *
     * @param application the application.
     */
    public StorageLookup(Application application) {
        this.application = application;
    }

    @Override
    public String lookup(String s) {
        return this.application.getStorageLocation(s).getAbsolutePath();
    }
}
