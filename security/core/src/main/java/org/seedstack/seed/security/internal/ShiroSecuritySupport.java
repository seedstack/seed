/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.seedstack.seed.security.AuthenticationException;
import org.seedstack.seed.security.AuthenticationToken;
import org.seedstack.seed.security.AuthorizationException;
import org.seedstack.seed.security.Role;
import org.seedstack.seed.security.Scope;
import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.security.SimpleScope;
import org.seedstack.seed.security.internal.authorization.ScopePermission;
import org.seedstack.seed.security.internal.authorization.SeedAuthorizationInfo;
import org.seedstack.seed.security.internal.realms.AuthenticationTokenWrapper;
import org.seedstack.seed.security.principals.PrincipalProvider;
import org.seedstack.seed.security.principals.Principals;
import org.seedstack.seed.security.principals.SimplePrincipalProvider;

class ShiroSecuritySupport implements SecuritySupport {
    @Inject
    private Set<Realm> realms;
    @Inject
    private SecurityManager securityManager;

    @Override
    public PrincipalProvider<?> getIdentityPrincipal() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.getPrincipal() instanceof PrincipalProvider) {
            return (PrincipalProvider<?>) subject.getPrincipal();
        }
        return Principals.identityPrincipal("");
    }

    @Override
    public Collection<PrincipalProvider<?>> getOtherPrincipals() {
        Collection<PrincipalProvider<?>> principals = new ArrayList<>();
        PrincipalCollection principalCollection = SecurityUtils.getSubject().getPrincipals();
        if (principalCollection == null) {
            return Collections.emptyList();
        }
        for (Object shiroPrincipal : principalCollection.asList()) {
            if (shiroPrincipal instanceof PrincipalProvider) {
                principals.add((PrincipalProvider<?>) shiroPrincipal);
            }
        }
        return principals;
    }

    @Override
    public <T> PrincipalProvider<T> getPrincipalByType(Class<T> principalClass) {
        return Principals.getOnePrincipalByType(getOtherPrincipals(), principalClass);
    }

    @Override
    public <T> Collection<PrincipalProvider<T>> getPrincipalsByType(Class<T> principalClass) {
        return Principals.getPrincipalsByType(getOtherPrincipals(), principalClass);
    }

    @Override
    public Collection<SimplePrincipalProvider> getSimplePrincipals() {
        return Principals.getSimplePrincipals(getOtherPrincipals());
    }

    @Override
    public SimplePrincipalProvider getSimplePrincipalByName(String principalName) {
        return Principals.getSimplePrincipalByName(getOtherPrincipals(), principalName);
    }

    @Override
    public boolean isAuthenticated() {
        return SecurityUtils.getSubject().isAuthenticated();
    }

    @Override
    public boolean isRemembered() {
        return SecurityUtils.getSubject().isRemembered();
    }

    @Override
    public boolean isPermitted(String permission) {
        return SecurityUtils.getSubject().isPermitted(permission);
    }

    @Override
    public boolean isPermitted(String permission, Scope... scopes) {
        if (scopes == null || scopes.length == 0) {
            return isPermitted(permission);
        }
        boolean isPermitted = true;
        for (Scope scope : scopes) {
            isPermitted = isPermitted && SecurityUtils.getSubject().isPermitted(new ScopePermission(permission, scope));
        }
        return isPermitted;
    }

    @Override
    public boolean isPermittedAll(String... permissions) {
        return SecurityUtils.getSubject().isPermittedAll(permissions);
    }

    @Override
    public boolean isPermittedAny(String... permissions) {
        for (boolean bool : SecurityUtils.getSubject().isPermitted(permissions)) {
            if (bool) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void checkPermission(String permission) {
        try {
            SecurityUtils.getSubject().checkPermission(permission);
        } catch (org.apache.shiro.authz.AuthorizationException e) {
            throw new AuthorizationException("Subject doesn't have permission " + permission, e);
        }
    }

    @Override
    public void checkPermission(String permission, Scope... scopes) {
        try {
            if (scopes == null || scopes.length == 0) {
                checkPermission(permission);
            } else {
                for (Scope scope : scopes) {
                    SecurityUtils.getSubject().checkPermission(new ScopePermission(permission, scope));
                }
            }
        } catch (org.apache.shiro.authz.AuthorizationException e) {
            throw new AuthorizationException("Subject doesn't have permission " + permission, e);
        }
    }

    @Override
    public void checkPermissions(String... stringPermissions) {
        Collection<org.apache.shiro.authz.Permission> permissions = new ArrayList<>();
        for (String stringPermission : stringPermissions) {
            permissions.add(new ScopePermission(stringPermission));
        }
        try {
            SecurityUtils.getSubject().checkPermissions(permissions);
        } catch (org.apache.shiro.authz.AuthorizationException e) {
            throw new AuthorizationException("Subject doesn't have permissions " + Arrays.toString(stringPermissions),
                    e);
        }
    }

    @Override
    public boolean hasRole(String roleIdentifier) {
        return SecurityUtils.getSubject().hasRole(roleIdentifier);
    }

    @Override
    public boolean hasRole(String roleIdentifier, Scope... scopes) {
        if (scopes == null || scopes.length == 0) {
            return hasRole(roleIdentifier);
        }
        Role role = null;
        Set<Role> roles = getRoles();
        for (Role role2 : roles) {
            if (role2.getName().equals(roleIdentifier)) {
                role = role2;
                break;
            }
        }
        if (role == null) {
            return false;
        }
        return role.getScopes().containsAll(Arrays.asList(scopes));
    }

    @Override
    public boolean hasAllRoles(String... roleIdentifiers) {
        return SecurityUtils.getSubject().hasAllRoles(Arrays.asList(roleIdentifiers));
    }

    @Override
    public boolean hasAnyRole(String... roleIdentifiers) {
        for (boolean hasRole : SecurityUtils.getSubject().hasRoles(Arrays.asList(roleIdentifiers))) {
            if (hasRole) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void checkRole(String roleIdentifier) {
        try {
            SecurityUtils.getSubject().checkRole(roleIdentifier);
        } catch (org.apache.shiro.authz.AuthorizationException e) {
            throw new AuthorizationException("Subject doesn't have role " + roleIdentifier, e);
        }
    }

    @Override
    public void checkRoles(String... roleIdentifiers) {
        try {
            SecurityUtils.getSubject().checkRoles(roleIdentifiers);
        } catch (org.apache.shiro.authz.AuthorizationException e) {
            throw new AuthorizationException("Subject doesn't have roles " + Arrays.toString(roleIdentifiers), e);
        }
    }

    @Override
    public void login(AuthenticationToken authenticationToken) {
        SecurityManager alreadyBoundSecurityManager = ThreadContext.getSecurityManager();
        try {
            if (alreadyBoundSecurityManager == null) {
                ThreadContext.bind(securityManager);
            }
            Subject currentSubject = SecurityUtils.getSubject();
            currentSubject.login(new AuthenticationTokenWrapper(authenticationToken));
        } catch (org.apache.shiro.authc.AuthenticationException e) {
            throw new AuthenticationException("Unable to login subject with provided credentials " + authenticationToken
                    .getPrincipal(), e);
        } finally {
            if (alreadyBoundSecurityManager == null) {
                ThreadContext.unbindSecurityManager();
            }
        }
    }

    @Override
    public void logout() {
        SecurityUtils.getSubject().logout();
    }

    private SeedAuthorizationInfo getAuthorizationInfo(Realm realm) {
        SeedAuthorizationInfo authzInfo = null;
        if (realm instanceof ShiroRealmAdapter) {
            AuthorizationInfo tmpInfo = ((ShiroRealmAdapter) realm).getAuthorizationInfo(
                    SecurityUtils.getSubject().getPrincipals());
            authzInfo = (SeedAuthorizationInfo) tmpInfo;
        }
        return authzInfo;
    }

    @Override
    public Set<Role> getRoles() {
        Set<Role> roles = new HashSet<>();
        for (Realm realm : realms) {
            SeedAuthorizationInfo authzInfo = getAuthorizationInfo(realm);
            if (authzInfo != null) {
                for (Role role : authzInfo.getObjectRoles()) {
                    roles.add(Role.unmodifiableRole(role));
                }
            }
        }
        return roles;
    }

    @Override
    public Set<SimpleScope> getSimpleScopes() {
        Set<SimpleScope> simpleScopes = new HashSet<>();
        for (Role role : getRoles()) {
            simpleScopes.addAll(role.getScopesByType(SimpleScope.class));
        }
        return simpleScopes;
    }

    @Override
    public String getHost() {
        Session s = SecurityUtils.getSubject().getSession(false);
        if (s == null) {
            return null;
        }
        return s.getHost();
    }

}
