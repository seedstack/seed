/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.spi;

import io.nuun.kernel.api.annotations.Facet;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import org.seedstack.seed.crypto.CryptoConfig;

/**
 * Provides access to the application SSL configuration.
 */
@Facet
public interface SSLProvider {

    /**
     * Provides an {@link javax.net.ssl.SSLContext} configured during the init phase.
     *
     * @return an SSL context, or null before the init phase
     */
    Optional<SSLContext> sslContext();

    /**
     * Provides the {@link CryptoConfig.SSLConfig} after the init phase.
     *
     * @return the SSL configuration, or null before the init phase
     */
    CryptoConfig.SSLConfig sslConfig();
}
