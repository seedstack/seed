/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it;

import org.junit.runner.RunWith;

/**
 * This class can be used as a base for integration tests that need to be recognized by SEED. Tests classes will be
 * bound by SEED on test initialization and will benefit of all SEED features (injection, aop interception, ...).
 *
 * @author redouane.loulou@ext.mpsa.com
 */
@RunWith(SeedITRunner.class)
public abstract class AbstractSeedIT {

}
