/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.authorization;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.seedstack.seed.security.Permission;
import org.seedstack.seed.security.Role;
import org.seedstack.seed.security.RolePermissionResolver;
import org.seedstack.seed.security.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads a configuration to resolve permissions of a role.
 */
public class ConfigurationRolePermissionResolver implements RolePermissionResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationRolePermissionResolver.class);
    private final Map<String, Set<String>> roles = new HashMap<>();

    @Override
    public Collection<Permission> resolvePermissionsInRole(Role role) {
        return roles.getOrDefault(role.getName(), Collections.emptySet()).stream().map(Permission::new).collect(
                Collectors.toList());
    }

    @Inject
    void readConfiguration(SecurityConfig securityConfig) {
        if (securityConfig.getPermissions().isEmpty()) {
            LOGGER.warn("{} defined, but its configuration is empty.", getClass().getSimpleName());
        } else {
            roles.clear();
            roles.putAll(securityConfig.getPermissions());
        }
    }
}
