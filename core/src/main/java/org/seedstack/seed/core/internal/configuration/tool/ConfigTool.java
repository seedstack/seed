/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.configuration.tool;

import java.util.Arrays;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.cli.CliArgs;
import org.seedstack.seed.core.internal.CoreErrorCode;

public class ConfigTool extends AbstractConfigTool {
    @CliArgs
    private String[] args;

    @Override
    public String toolName() {
        return "config";
    }

    @Override
    public Integer call() throws Exception {
        if (args != null && args.length > 0) {
            String[] path = String.join(".", (CharSequence[]) args).split("\\.");
            Node node = root.find(path);
            if (node == null) {
                info(path);
            } else {
                new TreePrinter(node).printTree(System.out);
            }
        } else {
            new TreePrinter(root).printTree(System.out);
        }
        return 0;
    }

    private void info(String[] path) {
        Node node = root.find(Arrays.copyOfRange(path, 0, path.length - 1));
        if (node == null) {
            throw SeedException.createNew(CoreErrorCode.INVALID_CONFIG_PATH).put("path", String.join(".", path));
        } else {
            PropertyInfo propertyInfo = node.getPropertyInfo(path[path.length - 1]);
            if (propertyInfo == null) {
                throw SeedException.createNew(CoreErrorCode.INVALID_CONFIG_PROPERTY).put("property",
                        path[path.length - 1]);
            }
            new DetailPrinter(propertyInfo).printDetail(System.out);
        }
    }
}
