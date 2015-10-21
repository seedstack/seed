/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.spi;

import io.nuun.kernel.api.annotations.Facet;

import javax.net.ssl.SSLContext;

/**
 * Provides access to the application SSL configuration.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@Facet
public interface SSLProvider {

    /**
     * Provides an {@link javax.net.ssl.SSLContext} configured during the init phase.
     *
     * @return an SSL context, or null before the init phase
     */
    SSLContext sslContext();

    /**
     * Provides the {@link SSLConfiguration} after the init phase.
     *
     * @return the SSL configuration, or null before the init phase
     */
    SSLConfiguration sslConfig();
}
