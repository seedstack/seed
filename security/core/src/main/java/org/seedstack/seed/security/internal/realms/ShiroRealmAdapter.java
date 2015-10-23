/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.realms;

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
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

import org.seedstack.seed.security.api.Realm;
import org.seedstack.seed.security.api.Role;
import org.seedstack.seed.security.api.PrincipalCustomizer;
import org.seedstack.seed.security.api.principals.PrincipalProvider;
import org.seedstack.seed.security.internal.authorization.SeedAuthorizationInfo;

/**
 * Realm adapter for Shiro.<br>
 * Uses a Realm from the api and is functional with Shiro.
 * 
 * @author yves.dautremay@mpsa.com
 */
public class ShiroRealmAdapter extends AuthorizingRealm {

    /**
     * Constructor with cache and credential matcher
     * 
     * @param cacheManager
     *            cache manager to use
     * @param matcher
     *            matcher to user
     */
    public ShiroRealmAdapter(CacheManager cacheManager, CredentialsMatcher matcher) {
        super(cacheManager, matcher);
    }

    /**
     * Constructor with cache
     * 
     * @param cacheManager
     *            cache manager to use
     */
    public ShiroRealmAdapter(CacheManager cacheManager) {
        super(cacheManager);
    }

    /**
     * Default constructor
     */
    public ShiroRealmAdapter() {
        super();
    }

    /** API realm */
    private Realm realm;
    
    @SuppressWarnings("rawtypes")
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
        Collection<PrincipalProvider<?>> principalProviders = new ArrayList<PrincipalProvider<?>>();
        principalProviders.add(idPrincipal);
        for (Object principal : principals) {
            if (principal instanceof PrincipalProvider) {
                principalProviders.add((PrincipalProvider<?>) principal);
            }
        }
        List<PrincipalProvider<?>> asList = principals.asList();
        for (Role role : realm.getRoleMapping().resolveRoles(realm.getRealmRoles(idPrincipal, asList), principalProviders)) {
            role.getPermissions().addAll(realm.getRolePermissionResolver().resolvePermissionsInRole(role));
            authzInfo.addRole(role);
        }
        return authzInfo;
    }

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) throws AuthenticationException {
		org.seedstack.seed.security.api.AuthenticationToken seedToken = convertToken(token);
		if(seedToken == null){
		    throw new UnsupportedTokenException("The token " + token.getClass() + " is not supported");
		}
		org.seedstack.seed.security.api.AuthenticationInfo apiAuthenticationInfo;
		try {
			apiAuthenticationInfo = realm.getAuthenticationInfo(seedToken);
		} catch (org.seedstack.seed.security.api.exceptions.IncorrectCredentialsException e) {
			throw new IncorrectCredentialsException(e);
		} catch (org.seedstack.seed.security.api.exceptions.UnknownAccountException e) {
			throw new UnknownAccountException(e);
		} catch (org.seedstack.seed.security.api.exceptions.UnsupportedTokenException e) {
			throw new UnsupportedTokenException(e);
		} catch (org.seedstack.seed.security.api.exceptions.AuthenticationException e) {
			throw new AuthenticationException(e);
		}

		SimpleAuthenticationInfo authcInfo = new SimpleAuthenticationInfo();
		SimplePrincipalCollection principals = new SimplePrincipalCollection(apiAuthenticationInfo.getIdentityPrincipal(), this.getName());
		authcInfo.setCredentials(token.getCredentials());
		//Realm principals
		for (PrincipalProvider<?> principal : apiAuthenticationInfo.getOtherPrincipals()) {
			principals.add(principal, this.getName());
		}
		//Custom principals
		for(PrincipalCustomizer<?> principalCustomizer : principalCustomizers){
		    if(principalCustomizer.supportedRealm().isAssignableFrom(getRealm().getClass())){
		        for(PrincipalProvider<?> principal : principalCustomizer.principalsToAdd(apiAuthenticationInfo.getIdentityPrincipal(), apiAuthenticationInfo.getOtherPrincipals())){
		            principals.add(principal, this.getName());
		        }
		    }
		}
		authcInfo.setPrincipals(principals);
		return authcInfo;
	}

	/**
	 * Getter realm
	 * 
	 * @return the realm
	 */
	public Realm getRealm() {
		return realm;
	}

    /**
     * Setter realm
     * 
     * @param realm the realm to set
     */
    public void setRealm(Realm realm) {
        this.realm = realm;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        org.seedstack.seed.security.api.AuthenticationToken seedToken = convertToken(token);
        return seedToken != null && realm.supportedToken().isAssignableFrom(seedToken.getClass());
    }
	
    private org.seedstack.seed.security.api.AuthenticationToken convertToken(AuthenticationToken token){
        if (token instanceof UsernamePasswordToken) {
            UsernamePasswordToken shiroToken = (UsernamePasswordToken)token;
            return new org.seedstack.seed.security.api.UsernamePasswordToken(shiroToken.getUsername(), shiroToken.getPassword());
        }else if (token instanceof AuthenticationTokenWrapper) {
            AuthenticationTokenWrapper shiroToken = (AuthenticationTokenWrapper)token;
            return shiroToken.getSeedToken();
        }else{
            return null;
        }
    }
	
}
