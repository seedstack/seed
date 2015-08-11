/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.mongodb.internal;

import com.google.inject.Module;
import org.apache.commons.configuration.Configuration;

interface MongoDbManager {
    void registerClient(String clientName, Configuration clientConfiguration);

    void registerDatabase(String clientName, String dbName, String alias);

    Module getModule();

    void shutdown();
}
