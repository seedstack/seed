/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.seedstack.seed.security.PrincipalCustomizer;
import org.seedstack.seed.security.Realm;
import org.seedstack.seed.security.Role;
import org.seedstack.seed.security.internal.authorization.SeedAuthorizationInfo;
import org.seedstack.seed.security.internal.realms.AuthenticationTokenWrapper;
import org.seedstack.seed.security.principals.PrincipalProvider;

class ShiroRealmAdapter extends AuthorizingRealm {
    private Realm realm;
    @Inject
    private Set<PrincipalCustomizer> principalCustomizers;

    @Override
    public AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
        return super.getAuthorizationInfo(principals);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SeedAuthorizationInfo authzInfo = new SeedAuthorizationInfo();
        PrincipalProvider<?> idPrincipal = (PrincipalProvider<?>) principals.getPrimaryPrincipal();
        Collection<PrincipalProvider<?>> principalProviders = new ArrayList<>();
        principalProviders.add(idPrincipal);
        for (Object principal : principals) {
            if (principal instanceof PrincipalProvider) {
                principalProviders.add((PrincipalProvider<?>) principal);
            }
        }
        List<PrincipalProvider<?>> asList = principals.asList();
        for (Role role : realm.getRoleMapping().resolveRoles(realm.getRealmRoles(idPrincipal, asList),
                principalProviders)) {
            role.getPermissions().addAll(realm.getRolePermissionResolver().resolvePermissionsInRole(role));
            authzInfo.addRole(role);
        }
        return authzInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
            final AuthenticationToken token) throws AuthenticationException {
        org.seedstack.seed.security.AuthenticationToken seedToken = convertToken(token);
        if (seedToken == null) {
            throw new UnsupportedTokenException("The token " + token.getClass() + " is not supported");
        }
        org.seedstack.seed.security.AuthenticationInfo apiAuthenticationInfo;
        try {
            apiAuthenticationInfo = realm.getAuthenticationInfo(seedToken);
        } catch (org.seedstack.seed.security.IncorrectCredentialsException e) {
            throw new IncorrectCredentialsException(e);
        } catch (org.seedstack.seed.security.UnknownAccountException e) {
            throw new UnknownAccountException(e);
        } catch (org.seedstack.seed.security.UnsupportedTokenException e) {
            throw new UnsupportedTokenException(e);
        } catch (org.seedstack.seed.security.AuthenticationException e) {
            throw new AuthenticationException(e);
        }

        SimpleAuthenticationInfo authcInfo = new SimpleAuthenticationInfo();
        SimplePrincipalCollection principals = new SimplePrincipalCollection(
                apiAuthenticationInfo.getIdentityPrincipal(), this.getName());
        authcInfo.setCredentials(token.getCredentials());
        //Realm principals
        for (PrincipalProvider<?> principal : apiAuthenticationInfo.getOtherPrincipals()) {
            principals.add(principal, this.getName());
        }
        //Custom principals
        for (PrincipalCustomizer<?> principalCustomizer : principalCustomizers) {
            if (principalCustomizer.supportedRealm().isAssignableFrom(getRealm().getClass())) {
                for (PrincipalProvider<?> principal : principalCustomizer.principalsToAdd(
                        apiAuthenticationInfo.getIdentityPrincipal(), apiAuthenticationInfo.getOtherPrincipals())) {
                    principals.add(principal, this.getName());
                }
            }
        }
        authcInfo.setPrincipals(principals);
        return authcInfo;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        org.seedstack.seed.security.AuthenticationToken seedToken = convertToken(token);
        return seedToken != null && realm.supportedToken().isAssignableFrom(seedToken.getClass());
    }

    @Override
    protected Object getAuthenticationCacheKey(AuthenticationToken token) {
        Object authenticationCacheKey = super.getAuthenticationCacheKey(token);
        if (authenticationCacheKey instanceof PrincipalProvider) {
            return ((PrincipalProvider) authenticationCacheKey).getPrincipal();
        } else {
            return authenticationCacheKey;
        }
    }

    @Override
    protected Object getAuthenticationCacheKey(PrincipalCollection principals) {
        Object authenticationCacheKey = super.getAuthenticationCacheKey(principals);
        if (authenticationCacheKey instanceof PrincipalProvider) {
            return ((PrincipalProvider) authenticationCacheKey).getPrincipal();
        } else {
            return authenticationCacheKey;
        }
    }

    @Override
    protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
        Object authenticationCacheKey = super.getAuthenticationCacheKey(principals);
        if (authenticationCacheKey instanceof PrincipalProvider) {
            return ((PrincipalProvider) authenticationCacheKey).getPrincipal();
        } else {
            return authenticationCacheKey;
        }
    }

    Realm getRealm() {
        return realm;
    }

    void setRealm(Realm realm) {
        this.realm = realm;
    }

    private org.seedstack.seed.security.AuthenticationToken convertToken(AuthenticationToken token) {
        if (token instanceof org.seedstack.seed.security.AuthenticationToken) {
            return (org.seedstack.seed.security.AuthenticationToken) token;
        } else if (token instanceof UsernamePasswordToken) {
            UsernamePasswordToken shiroToken = (UsernamePasswordToken) token;
            return new org.seedstack.seed.security.UsernamePasswordToken(shiroToken.getUsername(),
                    shiroToken.getPassword());
        } else if (token instanceof AuthenticationTokenWrapper) {
            AuthenticationTokenWrapper shiroToken = (AuthenticationTokenWrapper) token;
            return shiroToken.getSeedToken();
        } else {
            return null;
        }
    }
}
