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
import org.seedstack.seed.core.spi.configuration.ConfigurationLookup;

/**
 * This class resolve environment interpolations.
 *
 * @author adrien.lauer@mpsa.com
 */
@ConfigurationLookup("env")
public class EnvLookup extends StrLookup {
    @Override
    public String lookup(String key) {
        return System.getenv(key);
    }
}
