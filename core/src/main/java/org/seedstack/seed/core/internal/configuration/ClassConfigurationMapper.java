/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationMapper;
import org.seedstack.coffig.util.Utils;
import org.seedstack.seed.ClassConfiguration;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

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
    public Object map(TreeNode treeNode, Type type) {
        Class<?> rawType = Utils.getRawClass(((ParameterizedType) type).getActualTypeArguments()[0]);

        if (treeNode instanceof MapNode) {
            return ClassConfiguration.of(rawType, ((MapNode) treeNode).keys().stream()
                    .filter(key -> (treeNode.item(key) instanceof ValueNode))
                    .collect(toMap(
                            Function.identity(),
                            key -> treeNode.item(key).value()
                    ))
            );
        } else {
            throw SeedException.createNew(CoreErrorCode.INVALID_CLASS_CONFIGURATION)
                    .put("nodeType", treeNode.getClass().getSimpleName())
                    .put("class", rawType.getName());
        }
    }

    @Override
    public TreeNode unmap(Object object, Type type) {
        return null;
    }
}
