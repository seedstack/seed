/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.jms.internal;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import org.seedstack.seed.core.api.Logging;
import org.slf4j.Logger;

/**
 * Dummy exceptionListener for tests
 * @author redouane.loulou@ext.mpsa.com
 *
 */
public class MyExceptionListener implements ExceptionListener{

	@Logging
	Logger LOGGER;
	
	@Override
	public void onException(JMSException exception) {
		LOGGER.info(exception.getMessage(), exception);
	}

}
