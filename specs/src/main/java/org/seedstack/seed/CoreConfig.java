/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

import org.seedstack.coffig.Config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Config
public class CoreConfig {
    private Set<String> basePackages;

    public CoreConfig() {
        setBasePackages(new HashSet<>());
    }

    public Set<String> getBasePackages() {
        return Collections.unmodifiableSet(basePackages);
    }

    public void setBasePackages(Set<String> basePackages) {
        this.basePackages = new HashSet<>(basePackages);
    }

    public void addBasePackage(String basePackage) {
        basePackages.add(basePackage);
    }
}
