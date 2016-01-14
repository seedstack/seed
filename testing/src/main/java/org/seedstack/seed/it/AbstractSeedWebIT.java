/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.RunWith;

/**
 * This class can be used as a base for integration tests that need to run in a web container.
 *
 * @author adrien.lauer@mpsa.com
 */
@RunWith(Arquillian.class)
public abstract class AbstractSeedWebIT {
}
