/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.authorization;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.seedstack.seed.security.api.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.seedstack.seed.security.api.Domain;
import org.seedstack.seed.security.api.RoleMapping;
import org.seedstack.seed.security.api.principals.PrincipalProvider;

/**
 * Resolve the role mappings from an Configuration. Roles given to every user cans be defined by mapping it to
 * the GLOBAL_WILDCARD character.
 * This implementation manages domains :<br>
 * If mapping is titi.$DOMAIN$ = toto, tutu and given auth is titi.foo, then
 * returned roles will be toto and tutu, each role having a domain foo.
 * 
 * @author yves.dautremay@mpsa.com
 * 
 */
public class ConfigurationRoleMapping implements RoleMapping {

    /** wildcard used to give role to every user */
    private final static String GLOBAL_WILDCARD = "*";

    /** domain wildcard */
    private final static String DOMAIN_WILDCARD = "$DOMAIN$";

    /** section name */
    public static final String ROLES_SECTION_NAME = "roles";

    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationRoleMapping.class);

    /** map : role = mapped roles */
    private final Map<String, Set<String>> map = new HashMap<String, Set<String>>();
    
    /** roles given to every user */
    private final Set<String> givenRoles = new HashSet<String>();

    @Override
    public Collection<Role> resolveRoles(Set<String> auths, Collection<PrincipalProvider<?>> principalProviders) {
        Map<String, Role> roleMap = new HashMap<String, Role>();
        for (String role : givenRoles) {
			roleMap.put(role, new Role(role));
		}
        for (String auth : auths) {
            if (map.containsKey(auth)) {
                for (String roleName : map.get(auth)) {
                    if(!roleMap.containsKey(roleName)){
                        roleMap.put(roleName, new Role(roleName));
                    }
                }
            } else {
                // maybe a domain auth
                for (String mapKey : map.keySet()) {
                    String regexpMapKey = mapKey.replace(".", "\\.");
                    regexpMapKey = regexpMapKey.replace(DOMAIN_WILDCARD, ".*");
                    if (auth.matches(regexpMapKey)) {
                        String domainName = findDomain(mapKey, auth);
                        Set<String> foundRoleNames = map.get(mapKey);
                        for (String foundRoleName : foundRoleNames) {
                            Role currentRole = getOrCreateRoleInMap(foundRoleName, roleMap);
                            Domain domain = new Domain(domainName);
                            currentRole.getScopes().add(domain);
                        }
                    }
                }
            }
        }
        return roleMap.values();
    }
    
    private Role getOrCreateRoleInMap(String roleName, Map<String, Role> roleMap){
        Role currentRole;
        if (roleMap.containsKey(roleName)) {
            currentRole = roleMap.get(roleName);
        } else {
            currentRole = new Role(roleName);
            roleMap.put(roleName, currentRole);
        }
        return currentRole;
    }

    /**
     * Read the configuration to init role mappings
     * 
     * @param securityConfiguration
     *            configuration of security
     */
    @Inject
    public void readConfiguration(@Named("seed-security-config") Configuration securityConfiguration) {
        Configuration rolesConfiguration = securityConfiguration.subset(ROLES_SECTION_NAME);
        if (rolesConfiguration.isEmpty()) {
            LOGGER.warn("{} defined, but its configuration is empty.", getClass().getSimpleName());
            return;
        }
        map.clear();
        processRolesConfiguration(rolesConfiguration);
    }

    private void processRolesConfiguration(Configuration rolesConfiguration) {
        Iterator<String> keys = rolesConfiguration.getKeys();
        while (keys.hasNext()) {
            String roleName = keys.next();
            String[] perms = rolesConfiguration.getStringArray(roleName);
            for (String token : perms) {
            	if(GLOBAL_WILDCARD.equals(token)){
            		givenRoles.add(roleName);
            	}else{
            		Set<String> roles = map.get(token);
            		if(roles == null){
            			roles = new HashSet<String>();
            		}
            		roles.add(roleName);
            		map.put(token, roles);
            	}
            }
        }
    }

    /**
     * Finds the domain in a string that corresponds to a role with $DOMAIN$.<br>
     * For example, if wildcardAuth is toto.$DOMAIN$ and auth is toto.foo then
     * domain is foo.
     * 
     * @param wildcardAuth
     *            auth with $DOMAIN$
     * @param auth
     *            auth that corresponds
     * @return the domain.
     */
    private String findDomain(String wildcardAuth, String auth) {
        String domain;
        String before = StringUtils.substringBefore(wildcardAuth, DOMAIN_WILDCARD);
        String after = StringUtils.substringAfter(wildcardAuth, DOMAIN_WILDCARD);
        if (StringUtils.startsWith(wildcardAuth, DOMAIN_WILDCARD)) {
            domain = StringUtils.substringBefore(auth, after);
        } else if (StringUtils.endsWith(wildcardAuth, DOMAIN_WILDCARD)) {
            domain = StringUtils.substringAfter(auth, before);
        } else {
            domain = StringUtils.substringBetween(auth, before, after);
        }
        return domain;
    }
}
