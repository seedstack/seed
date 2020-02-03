/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import java.util.Arrays;
import org.seedstack.coffig.node.ArrayNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationProcessor;

public class SystemPropertiesProcessor implements ConfigurationProcessor {
    private static final String SYSTEM_PROPERTIES_CONFIG_PREFIX = "seedstack.config.";

    @Override
    public void process(MapNode configuration) {
        configuration.get("sys").ifPresent(treeNode -> {
            if (treeNode instanceof MapNode) {
                treeNode.namedNodes().forEach(namedNode -> {
                    if (namedNode.name().startsWith(SYSTEM_PROPERTIES_CONFIG_PREFIX)) {
                        String choppedKey = namedNode.name().substring(SYSTEM_PROPERTIES_CONFIG_PREFIX.length());
                        String value = namedNode.node().value();
                        if (value.contains(",")) {
                            String[] values = Arrays.stream(value.split(",")).map(String::trim).toArray(String[]::new);
                            configuration.set(choppedKey, new ArrayNode(values));
                        } else {
                            configuration.set(choppedKey, new ValueNode(value));
                        }
                    }
                });
            }
        });
    }
}
