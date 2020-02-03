/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import org.seedstack.seed.security.Realm;
import org.seedstack.seed.security.RoleMapping;
import org.seedstack.seed.security.RolePermissionResolver;

class RealmConfiguration {

    private final String name;

    private final Class<? extends Realm> realmClass;

    private Class<? extends RoleMapping> roleMappingClass;

    private Class<? extends RolePermissionResolver> rolePermissionResolverClass;

    RealmConfiguration(String name, Class<? extends Realm> realmClass) {
        this.name = name;
        this.realmClass = realmClass;
    }

    Class<? extends RoleMapping> getRoleMappingClass() {
        return roleMappingClass;
    }

    void setRoleMappingClass(Class<? extends RoleMapping> roleMappingClass) {
        this.roleMappingClass = roleMappingClass;
    }

    Class<? extends RolePermissionResolver> getRolePermissionResolverClass() {
        return rolePermissionResolverClass;
    }

    void setRolePermissionResolverClass(Class<? extends RolePermissionResolver> rolePermissionResolverClass) {
        this.rolePermissionResolverClass = rolePermissionResolverClass;
    }

    String getName() {
        return name;
    }

    Class<? extends Realm> getRealmClass() {
        return realmClass;
    }

}
