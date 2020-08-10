/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration.tool.fixtures;

import java.security.Permission;

    public final class ExitExceptionSecurityManager extends SecurityManager {

        @Override
        public void checkPermission(Permission perm) {
            // allow anything
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
            // allow anything
        }

        @Override
        public void checkExit(int status) {
            super.checkExit(status);
            throw new ExitException();
        }
}
