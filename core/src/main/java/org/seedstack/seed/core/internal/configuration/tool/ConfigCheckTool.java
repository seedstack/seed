/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.configuration.tool;

import org.seedstack.coffig.TreeNode;

public class ConfigCheckTool extends AbstractConfigTool {
    @Override
    public String toolName() {
        return "config-check";
    }

    @Override
    public Integer call() throws Exception {
        TreeNode tree = configuration.getTree();
        walk(tree, "");
        return 0;
    }

    private void walk(TreeNode tree, String path) {
        tree.namedNodes().forEach(nn -> {
            if (nn.node().type() == TreeNode.Type.VALUE_NODE) {
                System.out.println(path + "." + nn.name() + "=" + nn.node().value());
            } else {
                walk(nn.node(), path + "." + nn.name());
            }
        });
    }
}
