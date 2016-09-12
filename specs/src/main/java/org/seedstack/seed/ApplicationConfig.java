/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;

import java.io.File;
import java.util.UUID;

@Config("application")
public class ApplicationConfig {
    @SingleValue
    private String id = UUID.randomUUID().toString();
    private String name = id;
    private String version = "1.0.0";
    private File storage;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public File getStorage() {
        return storage;
    }

    public boolean isStorageEnabled() {
        return storage != null;
    }
}
