/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.authorization;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.mockito.Mockito;
import org.seedstack.seed.security.api.SecuritySupport;
import org.seedstack.seed.security.api.annotations.Logical;
import org.seedstack.seed.security.api.annotations.RequiresRoles;
import org.seedstack.seed.security.api.exceptions.AuthorizationException;

import static org.mockito.Mockito.when;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class RequiresRolesInterceptorTest {

    private RequiresRolesInterceptor underTest;

    @Test
    public void test_one_permission_ok() throws Throwable {
        SecuritySupport securitySupport = Mockito.mock(SecuritySupport.class);
        underTest = new RequiresRolesInterceptor(securitySupport);

        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);
        when(methodInvocation.getMethod()).thenReturn(RequiresRolesInterceptorTest.class.getMethod("securedMethod"));

        underTest.invoke(methodInvocation);
    }

    @Test(expected = AuthorizationException.class)
    public void test_one_permission_fail() throws Throwable {
        SecuritySupport securitySupport = Mockito.mock(SecuritySupport.class);
        Mockito.doThrow(new AuthorizationException()).when(securitySupport).checkRole("CODE");

        underTest = new RequiresRolesInterceptor(securitySupport);
        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);

        when(methodInvocation.getMethod()).thenReturn(RequiresRolesInterceptorTest.class.getMethod("securedMethod"));
        underTest.invoke(methodInvocation);
    }

    @Test
    public void test_or_permission_ok() throws Throwable {
        SecuritySupport securitySupport = Mockito.mock(SecuritySupport.class);
        Mockito.when(securitySupport.hasRole("CODE")).thenReturn(true);
        Mockito.when(securitySupport.hasRole("EAT")).thenReturn(false);

        underTest = new RequiresRolesInterceptor(securitySupport);
        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);

        when(methodInvocation.getMethod()).thenReturn(RequiresRolesInterceptorTest.class.getMethod("securedOrMethod"));
        underTest.invoke(methodInvocation);
    }

    @Test(expected = AuthorizationException.class)
    public void test_or_permission_fail() throws Throwable {
        SecuritySupport securitySupport = Mockito.mock(SecuritySupport.class);
        Mockito.when(securitySupport.hasRole("CODE")).thenReturn(false);
        Mockito.when(securitySupport.hasRole("EAT")).thenReturn(false);

        underTest = new RequiresRolesInterceptor(securitySupport);
        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);

        when(methodInvocation.getMethod()).thenReturn(RequiresRolesInterceptorTest.class.getMethod("securedOrMethod"));
        underTest.invoke(methodInvocation);
    }

    @Test
    public void test_and_permission_ok() throws Throwable {
        SecuritySupport securitySupport = Mockito.mock(SecuritySupport.class);
        Mockito.when(securitySupport.hasRole("CODE")).thenReturn(true);
        Mockito.when(securitySupport.hasRole("EAT")).thenReturn(true);

        underTest = new RequiresRolesInterceptor(securitySupport);
        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);

        when(methodInvocation.getMethod()).thenReturn(RequiresRolesInterceptorTest.class.getMethod("securedAndMethod"));
        underTest.invoke(methodInvocation);
    }

    @Test(expected = AuthorizationException.class)
    public void test_and_permission_fail() throws Throwable {
        SecuritySupport securitySupport = Mockito.mock(SecuritySupport.class);
        Mockito.doThrow(new AuthorizationException()).when(securitySupport).checkRoles("CODE", "EAT");

        underTest = new RequiresRolesInterceptor(securitySupport);
        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);

        when(methodInvocation.getMethod()).thenReturn(RequiresRolesInterceptorTest.class.getMethod("securedAndMethod"));
        underTest.invoke(methodInvocation);
    }

    @RequiresRoles("CODE")
    public void securedMethod() {
    }
    @RequiresRoles(value = {"CODE", "EAT"}, logical = Logical.OR)
    public void securedOrMethod() {
    }
    @RequiresRoles(value = {"CODE", "EAT"}, logical = Logical.AND)
    public void securedAndMethod() {
    }
}
