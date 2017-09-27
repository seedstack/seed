/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.configuration;

import java.util.Locale;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.spi.ConfigurationProcessor;

public class SecureConfigurationProcessor implements ConfigurationProcessor {
    @Override
    public void process(MapNode configuration) {
        configuration.get("env").ifPresent(TreeNode::hide);
        configuration.get("sys").ifPresent(TreeNode::hide);
        configuration.walk()
                .filter(node -> node.type() == TreeNode.Type.MAP_NODE)
                .forEach(node -> node.namedNodes()
                        .filter(this::isPotentialPassword)
                        .forEach(namedNode -> namedNode.node().hide())
                );
    }

    private boolean isPotentialPassword(NamedNode namedNode) {
        String key = namedNode.name().toUpperCase(Locale.ENGLISH);
        return key.contains("PASSWORD") || key.contains("PASSWD") || key.contains("PWD");
    }
}
