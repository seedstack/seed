/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.util.ThreadState;
import org.junit.AfterClass;

/**
 * Abstract test case enabling Shiro in test environments.
 */
//TODO:find a suitable package for this helper class provided by shiro
public abstract class AbstractShiroTest {

  private static ThreadState subjectThreadState;

  public AbstractShiroTest() {
  }

  /**
   * Allows subclasses to set the currently executing {@link Subject} instance.
   *
   * @param subject
   *          the Subject instance
   */
  protected void setSubject(Subject subject) {
    clearSubject();
    subjectThreadState = createThreadState(subject);
    subjectThreadState.bind();
  }

  protected Subject getSubject() {
    return SecurityUtils.getSubject();
  }

  protected ThreadState createThreadState(Subject subject) {
    return new SubjectThreadState(subject);
  }

  /**
   * Clears Shiro's thread state, ensuring the thread remains clean for future test execution.
   */
  protected void clearSubject() {
    doClearSubject();
  }

  private static void doClearSubject() {
    if (subjectThreadState != null) {
      subjectThreadState.clear();
      subjectThreadState = null;
    }
  }

  protected static void setSecurityManager(SecurityManager securityManager) {
    SecurityUtils.setSecurityManager(securityManager);
  }

  protected static SecurityManager getSecurityManager() {
    return SecurityUtils.getSecurityManager();
  }

  @AfterClass
  public static void tearDownShiro() {
    doClearSubject();
    try {
      SecurityManager securityManager = getSecurityManager();
      LifecycleUtils.destroy(securityManager);
    } catch (UnavailableSecurityManagerException e) {
      // we don't care about this when cleaning up the test environment
      // (for example, maybe the subclass is a unit test and it didn't
      // need a SecurityManager instance because it was using only
      // mock Subject instances)
    }
    setSecurityManager(null);
  }
}
