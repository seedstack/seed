/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.mail.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.seedstack.seed.core.api.Install;
import org.seedstack.seed.mail.api.MessageRetriever;
import org.seedstack.seed.mail.assertions.MailMessagesAssertions;
import org.subethamail.wiser.Wiser;

@Install
class MockMailServerModule extends AbstractModule {
    @Override
    protected void configure() {
        requestStaticInjection(MailMessagesAssertions.class);

        bind(Wiser.class).in(Scopes.SINGLETON);
        bind(MessageRetriever.class).to(WiserMessageRetriever.class).in(Scopes.SINGLETON);
    }
}
