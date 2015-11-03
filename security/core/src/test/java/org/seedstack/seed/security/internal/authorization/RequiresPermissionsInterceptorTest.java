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
import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.security.Logical;
import org.seedstack.seed.security.RequiresPermissions;
import org.seedstack.seed.security.AuthorizationException;

import static org.mockito.Mockito.when;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class RequiresPermissionsInterceptorTest {

    private RequiresPermissionsInterceptor underTest;

    @Test
    public void test_one_permission_ok() throws Throwable {
        SecuritySupport securitySupport = Mockito.mock(SecuritySupport.class);
        underTest = new RequiresPermissionsInterceptor(securitySupport);

        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);
        when(methodInvocation.getMethod()).thenReturn(RequiresPermissionsInterceptorTest.class.getMethod("securedMethod"));

        underTest.invoke(methodInvocation);
    }

    @Test(expected = AuthorizationException.class)
    public void test_one_permission_fail() throws Throwable {
        SecuritySupport securitySupport = Mockito.mock(SecuritySupport.class);
        Mockito.doThrow(new AuthorizationException()).when(securitySupport).checkPermission("CODE");

        underTest = new RequiresPermissionsInterceptor(securitySupport);
        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);

        when(methodInvocation.getMethod()).thenReturn(RequiresPermissionsInterceptorTest.class.getMethod("securedMethod"));
        underTest.invoke(methodInvocation);
    }

    @Test
    public void test_or_permission_ok() throws Throwable {
        SecuritySupport securitySupport = Mockito.mock(SecuritySupport.class);
        Mockito.when(securitySupport.isPermitted("CODE")).thenReturn(true);
        Mockito.when(securitySupport.isPermitted("EAT")).thenReturn(false);

        underTest = new RequiresPermissionsInterceptor(securitySupport);
        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);

        when(methodInvocation.getMethod()).thenReturn(RequiresPermissionsInterceptorTest.class.getMethod("securedOrMethod"));
        underTest.invoke(methodInvocation);
    }

    @Test(expected = AuthorizationException.class)
    public void test_or_permission_fail() throws Throwable {
        SecuritySupport securitySupport = Mockito.mock(SecuritySupport.class);
        Mockito.when(securitySupport.isPermitted("CODE")).thenReturn(false);
        Mockito.when(securitySupport.isPermitted("EAT")).thenReturn(false);

        underTest = new RequiresPermissionsInterceptor(securitySupport);
        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);

        when(methodInvocation.getMethod()).thenReturn(RequiresPermissionsInterceptorTest.class.getMethod("securedOrMethod"));
        underTest.invoke(methodInvocation);
    }

    @Test
    public void test_and_permission_ok() throws Throwable {
        SecuritySupport securitySupport = Mockito.mock(SecuritySupport.class);
        Mockito.when(securitySupport.isPermitted("CODE")).thenReturn(true);
        Mockito.when(securitySupport.isPermitted("EAT")).thenReturn(true);

        underTest = new RequiresPermissionsInterceptor(securitySupport);
        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);

        when(methodInvocation.getMethod()).thenReturn(RequiresPermissionsInterceptorTest.class.getMethod("securedAndMethod"));
        underTest.invoke(methodInvocation);
    }

    @Test(expected = AuthorizationException.class)
    public void test_and_permission_fail() throws Throwable {
        SecuritySupport securitySupport = Mockito.mock(SecuritySupport.class);
        Mockito.doThrow(new AuthorizationException()).when(securitySupport).checkPermissions("CODE", "EAT");

        underTest = new RequiresPermissionsInterceptor(securitySupport);
        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);

        when(methodInvocation.getMethod()).thenReturn(RequiresPermissionsInterceptorTest.class.getMethod("securedAndMethod"));
        underTest.invoke(methodInvocation);
    }

    @RequiresPermissions("CODE")
    public void securedMethod() {
    }
    @RequiresPermissions(value = {"CODE", "EAT"}, logical = Logical.OR)
    public void securedOrMethod() {
    }
    @RequiresPermissions(value = {"CODE", "EAT"}, logical = Logical.AND)
    public void securedAndMethod() {
    }
}
