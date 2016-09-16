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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.File;
import java.util.UUID;

@Config("application")
public class ApplicationConfig {
    @SingleValue
    @NotNull
    @Size(min = 1)
    private String id = UUID.randomUUID().toString();
    @NotNull
    @Size(min = 1)
    private String name = id;
    @NotNull
    @Size(min = 1)
    private String version = "1.0.0";
    private File storage;

    public String getId() {
        return id;
    }

    public ApplicationConfig setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ApplicationConfig setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ApplicationConfig setVersion(String version) {
        this.version = version;
        return this;
    }

    public File getStorage() {
        return storage;
    }

    public ApplicationConfig setStorage(File storage) {
        this.storage = storage;
        return this;
    }

    public boolean isStorageEnabled() {
        return storage != null;
    }
}
