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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Config
public class CoreConfig {
    @NotNull
    private Set<String> basePackages = new HashSet<>();

    public Set<String> getBasePackages() {
        return Collections.unmodifiableSet(basePackages);
    }

    public CoreConfig addBasePackage(String basePackage) {
        basePackages.add(basePackage);
        return this;
    }
}
