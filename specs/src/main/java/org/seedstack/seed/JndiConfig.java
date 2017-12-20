/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.seedstack.coffig.Config;

@Config("jndi")
public class JndiConfig {
    @NotNull
    private Map<String, String> additionalContexts = new HashMap<>();

    public Map<String, String> getAdditionalContexts() {
        return Collections.unmodifiableMap(additionalContexts);
    }

    public JndiConfig addAdditionalContext(String name, String propertiesPath) {
        this.additionalContexts.put(name, propertiesPath);
        return this;
    }
}
