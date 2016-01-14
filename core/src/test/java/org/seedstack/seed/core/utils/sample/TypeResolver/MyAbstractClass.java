/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils.sample.TypeResolver;

import java.util.Collection;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 23/09/2014
 */
public abstract class MyAbstractClass<H, I extends Collection<?>> implements MyInterface<I, Long, H> {
}
