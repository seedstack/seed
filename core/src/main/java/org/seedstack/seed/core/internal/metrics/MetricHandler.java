/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.metrics;

import com.codahale.metrics.Metric;

/**
 * Handler to create a new {@link Metric} object.
 * @author thierry.bouvet@mpsa.com
 *
 */
public interface MetricHandler {

	/**
	 * @return a {@link Metric}
	 */
	Metric handle();
	
}
