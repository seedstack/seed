/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

import org.seedstack.coffig.Config;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Config("application")
public class ApplicationConfig {
    @NotNull
    private Set<String> basePackages = new HashSet<>();
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
    private boolean packageScanWarning = true;
    private boolean printBanner = true;
    private ColorOutput colorOutput = ColorOutput.AUTODETECT;

    public String getId() {
        return id;
    }

    public ApplicationConfig setId(String id) {
        if (this.id.equals(this.name)) {
            // keep name and id in sync if no custom name was specified
            this.name = id;
        }
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

    public Set<String> getBasePackages() {
        return Collections.unmodifiableSet(basePackages);
    }

    public ApplicationConfig addBasePackage(String basePackage) {
        this.basePackages.add(basePackage);
        return this;
    }

    public boolean isPackageScanWarning() {
        return packageScanWarning;
    }

    public ApplicationConfig setPackageScanWarning(boolean packageScanWarning) {
        this.packageScanWarning = packageScanWarning;
        return this;
    }

    public boolean isPrintBanner() {
        return printBanner;
    }

    public ApplicationConfig setPrintBanner(boolean printBanner) {
        this.printBanner = printBanner;
        return this;
    }

    public ColorOutput getColorOutput() {
        return colorOutput;
    }

    public ApplicationConfig setColorOutput(ColorOutput colorOutput) {
        this.colorOutput = colorOutput;
        return this;
    }

    public enum ColorOutput {
        AUTODETECT,
        PASSTHROUGH,
        ENABLE,
        DISABLE
    }
}
