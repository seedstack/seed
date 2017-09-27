/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
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
 */
@RunWith(SeedITRunner.class)
public abstract class AbstractSeedIT {

}
