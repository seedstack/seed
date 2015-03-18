/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.ldap.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.UnknownAccountException;
import org.seedstack.seed.core.api.Configuration;
import org.seedstack.seed.security.ldap.api.LDAPException;
import org.seedstack.seed.security.ldap.api.LDAPSupport;
import org.seedstack.seed.security.ldap.api.LDAPUserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

public class DefaultLDAPSupport implements LDAPSupport {

    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultLDAPSupport.class);

    @Configuration("org.seedstack.seed.security.ldap.user-base")
    private String[] userBase;

    @Configuration(value = "org.seedstack.seed.security.ldap.user-class", mandatory = false)
    private String userObjectClass;

    @Configuration(value = "org.seedstack.seed.security.ldap.user-identity-attribute", defaultValue = "uid")
    private String userIdentityAttribute;

    @Configuration(value = "org.seedstack.seed.security.ldap.user-additional-attributes", mandatory = false)
    private String[] userAdditionalAttributes;

    @Configuration("org.seedstack.seed.security.ldap.group-base")
    private String[] groupBase;

    @Configuration(value = "org.seedstack.seed.security.ldap.group-class", mandatory = false)
    private String groupObjectClass;

    @Configuration(value = "org.seedstack.seed.security.ldap.group-member-attribute", defaultValue = "member")
    private String groupMemberAttribute;

    @Inject
    LDAPConnectionPool ldapConnectionPool;

    @Override
    public LDAPUserContext createUserContext(String dn) {
        return internalCreateUser(dn);
    }

    private DefaultLDAPUserContext internalCreateUser(String dn) {
        return new DefaultLDAPUserContext(dn);
    }

    @Override
    public LDAPUserContext findUser(String identityAttributeValue) throws LDAPException {
        try {
            Filter userClassFilter;
            if (userObjectClass != null && !userObjectClass.isEmpty()) {
                userClassFilter = Filter.createEqualityFilter("objectClass", userObjectClass);
            } else {
                userClassFilter = Filter.createPresenceFilter("objectClass");
            }
            Filter filter = Filter.createANDFilter(userClassFilter, Filter.createEqualityFilter(userIdentityAttribute, identityAttributeValue));
            LOGGER.debug(filter.toString());
            String[] attributesToRetrieve;
            if (userAdditionalAttributes != null) {
                attributesToRetrieve = userAdditionalAttributes;
                if (!ArrayUtils.contains(attributesToRetrieve, "cn") || !ArrayUtils.contains(attributesToRetrieve, "CN"))
                    ArrayUtils.add(attributesToRetrieve, "cn");
            } else
                attributesToRetrieve = new String[] { "cn" };
            SearchResult searchResult = ldapConnectionPool.search(StringUtils.join(userBase, ','), SearchScope.SUB, filter, attributesToRetrieve);
            if (searchResult.getEntryCount() != 1) {
                throw new UnknownAccountException();
            }
            SearchResultEntry searchResultEntry = searchResult.getSearchEntries().get(0);
            String dn = searchResultEntry.getDN();
            DefaultLDAPUserContext ldapUserContext = internalCreateUser(dn);
            ldapUserContext.getKnownAttributes().put("cn", searchResultEntry.getAttributeValue("cn"));
            return ldapUserContext;
        } catch (com.unboundid.ldap.sdk.LDAPException e) {
            throw new LDAPException(e);
        }
    }

    @Override
    public void authenticate(LDAPUserContext userContext, String password) throws LDAPException {
        try {
            ldapConnectionPool.bindAndRevertAuthentication(userContext.getDn(), password);
        } catch (com.unboundid.ldap.sdk.LDAPException e) {
            throw new LDAPException(e);
        }
    }

    @Override
    public String getAttributeValue(LDAPUserContext userContext, String attribute) throws LDAPException {
        if (((DefaultLDAPUserContext) userContext).getKnownAttributes().get(attribute.toLowerCase()) != null) {
            return ((DefaultLDAPUserContext) userContext).getKnownAttributes().get(attribute.toLowerCase());
        }
        return getAttributeValues(userContext, attribute).get(attribute);
    }

    @Override
    public Map<String, String> getAttributeValues(LDAPUserContext userContext, String... attributes) throws LDAPException {
        Map<String, String> result = new HashMap<String, String>();
        List<String> retainedAttr = new ArrayList<String>();
        Map<String, String> knownAttributes = ((DefaultLDAPUserContext) userContext).getKnownAttributes();
        for (String attr : attributes) {
            if (knownAttributes.get(attr.toLowerCase()) == null) {
                retainedAttr.add(attr.toLowerCase());
            }
        }
        if (!retainedAttr.isEmpty()) {
            LOGGER.debug("Will connect to LDAP to retrieve attributes {}", retainedAttr);
            try {
                SearchResultEntry entry = ldapConnectionPool.getEntry(userContext.getDn(), retainedAttr.toArray(new String[retainedAttr.size()]));
                for (String attr : retainedAttr) {
                    knownAttributes.put(attr, entry.getAttributeValue(attr));
                }
            } catch (com.unboundid.ldap.sdk.LDAPException e) {
                throw new LDAPException(e);
            }
        }
        for (String attr : attributes) {
            result.put(attr.toLowerCase(), knownAttributes.get(attr.toLowerCase()));
        }
        return result;
    }

    @Override
    public Set<String> retrieveUserGroups(LDAPUserContext userContext) throws LDAPException {
        Set<String> groups = new HashSet<String>();
        try {
            Filter groupClassFilter;
            if (groupObjectClass != null && !groupObjectClass.isEmpty()) {
                groupClassFilter = Filter.createEqualityFilter("objectClass", groupObjectClass);
            } else {
                groupClassFilter = Filter.createPresenceFilter("objectClass");
            }
            Filter filter = Filter.createANDFilter(groupClassFilter, Filter.createEqualityFilter(groupMemberAttribute, userContext.getDn()));
            LOGGER.debug(filter.toString());
            SearchResult searchResult = ldapConnectionPool.search(StringUtils.join(groupBase, ','), SearchScope.SUB, filter, "cn");
            for (SearchResultEntry entry : searchResult.getSearchEntries()) {
                groups.add(entry.getAttributeValue("cn"));
            }
            return groups;
        } catch (com.unboundid.ldap.sdk.LDAPException e) {
            throw new LDAPException(e);
        }
    }
}
