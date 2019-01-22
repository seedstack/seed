/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import java.io.Serializable;

/**
 * A Permission is represented by a String witch describes actions that can be
 * done on a type of objects. A Permission can be limited to one or more
 * {@link Scope}s.
 * <p>
 * Some examples of Permissions :
 * <ul>
 * <li>door:close,open</li>
 * <li>document:print</li>
 * <li>user:modify:user-id</li>
 * </ul>
 */
public class Permission implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String permission;

    /**
     * New Permission given its String representation
     *
     * @param permission the string representation
     */
    public Permission(String permission) {
        this.permission = permission;
    }

    /**
     * Getter permission
     *
     * @return the String representation of the permission
     */
    public String getPermission() {
        return permission;
    }
}
