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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
* @author Pierre Thirouin <pierre.thirouin@ext.mpsa.com>
*         14/11/2014
*/
public class FakeConnectionFactory implements ConnectionFactory {
    @Override
    public Connection createConnection() throws JMSException {
        throw new JMSException("Connection closed");
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        throw new JMSException("Connection closed");
    }
}
