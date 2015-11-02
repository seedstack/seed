/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.authorization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.security.Permission;
import org.seedstack.seed.security.Role;
import org.seedstack.seed.security.RolePermissionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads a configuration to resolve permissions of a role.
 * 
 * @author yves.dautremay@mpsa.com
 * 
 */
public class ConfigurationRolePermissionResolver implements RolePermissionResolver {

    /** name of section */
    public static final String PERMISSIONS_SECTION_NAME = "permissions";

    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationRolePermissionResolver.class);

    /** map roleName = permissions */
    private final Map<String, Set<String>> roles = new HashMap<String, Set<String>>();

    @Override
    public Collection<Permission> resolvePermissionsInRole(Role role) {
        Set<String> permissionsSet = roles.get(role.getName());
        if (permissionsSet == null) {
            return Collections.emptyList();
        }
        Collection<Permission> permissions = new ArrayList<Permission>(permissionsSet.size());
        for (String permission : permissionsSet) {
            permissions.add(new Permission(permission));
        }
        return permissions;
    }

    /**
     * Reads the configuration to init role permission mappings
     * 
     * @param securityConfiguration
     *            configuration of security
     */
    @Inject
    public void readConfiguration(@Named("seed-security-config") Configuration securityConfiguration) {
        Configuration permissionsConfiguration = securityConfiguration.subset(PERMISSIONS_SECTION_NAME);
        if (permissionsConfiguration.isEmpty()) {
            LOGGER.warn("{} defined, but its configuration is empty.", getClass().getSimpleName());
            return;
        }
        roles.clear();
        processPermissionsConfiguration(permissionsConfiguration);
    }

    private void processPermissionsConfiguration(Configuration permissionsConfiguration) {
        Iterator<String> keys = permissionsConfiguration.getKeys();
        while (keys.hasNext()) {
            String roleName = keys.next();
            String[] tokens = permissionsConfiguration.getStringArray(roleName);
            Set<String> permissions = new HashSet<String>();
            for (String permission : tokens) {
                permissions.add(permission);
            }
            roles.put(roleName, permissions);
        }
    }
}
