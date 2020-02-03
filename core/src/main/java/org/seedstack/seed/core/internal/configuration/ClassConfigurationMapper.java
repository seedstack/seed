/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import static org.seedstack.shed.reflect.Types.rawClassOf;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.spi.ConfigurationMapper;
import org.seedstack.seed.ClassConfiguration;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;

public class ClassConfigurationMapper implements ConfigurationMapper {
    @Override
    public boolean canHandle(Type type) {
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                return ClassConfiguration.class.isAssignableFrom(((Class<?>) rawType));
            }
        }
        return false;
    }

    @Override
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Cast is verified in canHandle() method")
    public Object map(TreeNode treeNode, Type type) {
        Class<?> rawType = rawClassOf(((ParameterizedType) type).getActualTypeArguments()[0]);
        if (treeNode.type() == TreeNode.Type.MAP_NODE) {
            Map<String, String> result = new HashMap<>();
            treeNode.namedNodes()
                    .filter(namedNode -> isValueNode(namedNode.node()))
                    .forEach(namedNode -> result.put(namedNode.name(), namedNode.node().value()));
            return ClassConfiguration.of(rawType, result);
        } else {
            throw SeedException.createNew(CoreErrorCode.INVALID_CLASS_CONFIGURATION)
                    .put("nodeType", treeNode.type())
                    .put("class", rawType.getName());
        }
    }

    @Override
    public TreeNode unmap(Object object, Type type) {
        return null;
    }

    private boolean isValueNode(TreeNode treeNode) {
        return treeNode != null && treeNode.type() == TreeNode.Type.VALUE_NODE;
    }
}
