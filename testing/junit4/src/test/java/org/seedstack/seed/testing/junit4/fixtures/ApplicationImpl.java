/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.junit4.fixtures;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.provider.InMemoryProvider;
import org.seedstack.seed.Application;
import org.seedstack.seed.ClassConfiguration;

class ApplicationImpl implements Application {
    private static final String CONFIG_PREFIX = "seedstack.config.";
    private final Coffig coffig;
    private final Map<String, String> kernelParameters;
    private final String[] args;

    ApplicationImpl(String[] args, Map<String, String> kernelParameters) {
        this.kernelParameters = kernelParameters;
        this.args = args;
        InMemoryProvider provider = new InMemoryProvider();
        for (Map.Entry<String, String> kernelParam : kernelParameters.entrySet()) {
            String key = kernelParam.getKey();
            if (key.startsWith(CONFIG_PREFIX)) {
                String choppedKey = key.substring(CONFIG_PREFIX.length());
                provider.put(choppedKey, kernelParam.getValue());
            }
        }
        coffig = Coffig.builder().withProviders(provider).build();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public File getStorageLocation(String context) {
        return null;
    }

    @Override
    public boolean isStorageEnabled() {
        return false;
    }

    @Override
    public Coffig getConfiguration() {
        return coffig;
    }

    @Override
    public <T> ClassConfiguration<T> getConfiguration(Class<T> someClass) {
        return null;
    }

    @Override
    public String substituteWithConfiguration(String value) {
        return null;
    }

    @Override
    public Map<String, String> getKernelParameters() {
        return Collections.unmodifiableMap(kernelParameters);
    }

    @Override
    public String[] getArguments() {
        return args.clone();
    }
}
