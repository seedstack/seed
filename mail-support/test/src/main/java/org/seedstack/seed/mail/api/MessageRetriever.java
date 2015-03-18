/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.mail.api;

import javax.mail.Message;

/**
 * This a helper interface for retrieving messages that where sent to the Wiser Server
 * its only purpose is alleviate the user from knowing about the MockServer used, after all
 * its only need is to get back the messages that were sent in order to make some assertions
 * to validate their integrity.
 *
 * @author aymen.benhmida@ext.mpsa.com
 */
public interface MessageRetriever {
    /**
     * @return the sent messages.
     */
    java.util.Collection<Message> getSentMessages();
}
