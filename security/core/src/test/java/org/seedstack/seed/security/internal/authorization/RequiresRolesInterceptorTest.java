/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal.authorization;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.seedstack.seed.security.AuthorizationException;
import org.seedstack.seed.security.Logical;
import org.seedstack.seed.security.RequiresRoles;
import org.seedstack.seed.security.internal.shiro.AbstractShiroTest;

public class RequiresRolesInterceptorTest extends AbstractShiroTest {

    private RequiresRolesInterceptor underTest;
    private Subject subjectUnderTest;

    @Before
    public void setup() {
        subjectUnderTest = Mockito.mock(Subject.class);
        setSubject(subjectUnderTest);
    }

    @After
    public void tearDownSubject() {
        clearSubject();
    }

    @Test
    public void test_one_permission_ok() throws Throwable {
        underTest = new RequiresRolesInterceptor();
        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);
        when(methodInvocation.getMethod())
                .thenReturn(RequiresRolesInterceptorTest.class.getMethod("securedMethod"));

        underTest.invoke(methodInvocation);
    }

    @Test(expected = AuthorizationException.class)
    public void test_one_permission_fail() throws Throwable {
        Mockito.doThrow(new AuthorizationException()).when(subjectUnderTest).checkRole("CODE");

        underTest = new RequiresRolesInterceptor();
        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);
        when(methodInvocation.getMethod())
                .thenReturn(RequiresRolesInterceptorTest.class.getMethod("securedMethod"));
        underTest.invoke(methodInvocation);
    }

    @Test
    public void test_or_permission_ok() throws Throwable {
        Mockito.when(subjectUnderTest.hasRole("CODE")).thenReturn(true);
        Mockito.when(subjectUnderTest.hasRole("EAT")).thenReturn(false);

        underTest = new RequiresRolesInterceptor();
        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);

        when(methodInvocation.getMethod())
                .thenReturn(RequiresRolesInterceptorTest.class.getMethod("securedOrMethod"));
        underTest.invoke(methodInvocation);
    }

    @Test(expected = AuthorizationException.class)
    public void test_or_permission_fail() throws Throwable {
        Mockito.when(subjectUnderTest.hasRole("CODE")).thenReturn(false);
        Mockito.when(subjectUnderTest.hasRole("EAT")).thenReturn(false);

        underTest = new RequiresRolesInterceptor();
        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);

        when(methodInvocation.getMethod())
                .thenReturn(RequiresRolesInterceptorTest.class.getMethod("securedOrMethod"));
        underTest.invoke(methodInvocation);
    }

    @Test
    public void test_and_permission_ok() throws Throwable {

        List<String> roles = new ArrayList<>();
        roles.add("CODE");
        roles.add("EAT");
        Mockito.when(subjectUnderTest.hasAllRoles(roles)).thenReturn(true);

        underTest = new RequiresRolesInterceptor();
        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);

        when(methodInvocation.getMethod())
                .thenReturn(RequiresRolesInterceptorTest.class.getMethod("securedAndMethod"));
        underTest.invoke(methodInvocation);
    }

    @Test(expected = AuthorizationException.class)
    public void test_and_permission_fail() throws Throwable {
        Mockito.doThrow(new AuthorizationException()).when(subjectUnderTest).checkRoles("CODE", "EAT");

        underTest = new RequiresRolesInterceptor();
        MethodInvocation methodInvocation = Mockito.mock(MethodInvocation.class);

        when(methodInvocation.getMethod())
                .thenReturn(RequiresRolesInterceptorTest.class.getMethod("securedAndMethod"));
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
