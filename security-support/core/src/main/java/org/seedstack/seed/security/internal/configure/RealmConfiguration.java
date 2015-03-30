/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.configure;

import org.seedstack.seed.security.api.Realm;
import org.seedstack.seed.security.api.RoleMapping;
import org.seedstack.seed.security.api.RolePermissionResolver;

/**
 * Represents a realm as read in the configuration
 * 
 * @author yves.dautremay@mpsa.com
 */
public class RealmConfiguration {

    private String name;

    private Class<? extends Realm> realmClass;

    private Class<? extends RoleMapping> roleMappingClass;

    private Class<? extends RolePermissionResolver> rolePermissionResolverClass;

    /**
     * Constructor
     * 
     * @param name The realm name.
     * @param realmClass The realm class.
     */
    public RealmConfiguration(String name, Class<? extends Realm> realmClass) {
        this.name = name;
        this.realmClass = realmClass;
    }

    /**
     * Getter roleMappingClass
     * 
     * @return the roleMappingClass
     */
    public Class<? extends RoleMapping> getRoleMappingClass() {
        return roleMappingClass;
    }

    /**
     * Setter roleMappingClass
     * 
     * @param roleMappingClass
     *            the roleMappingClass to set
     */
    public void setRoleMappingClass(Class<? extends RoleMapping> roleMappingClass) {
        this.roleMappingClass = roleMappingClass;
    }

    /**
     * Getter rolePermissionResolverClass
     * 
     * @return the rolePermissionResolverClass
     */
    public Class<? extends RolePermissionResolver> getRolePermissionResolverClass() {
        return rolePermissionResolverClass;
    }

    /**
     * Setter rolePermissionResolverClass
     * 
     * @param rolePermissionResolverClass
     *            the rolePermissionResolverClass to set
     */
    public void setRolePermissionResolverClass(Class<? extends RolePermissionResolver> rolePermissionResolverClass) {
        this.rolePermissionResolverClass = rolePermissionResolverClass;
    }

    /**
     * Getter name
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter realmClass
     * 
     * @return the realmClass
     */
    public Class<? extends Realm> getRealmClass() {
        return realmClass;
    }

}
