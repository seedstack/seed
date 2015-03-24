/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 27 févr. 2015
 */
package org.seedstack.seed.crypto.internal;

import io.nuun.kernel.core.AbstractPlugin;

/**
 * Plugin for cryptography support
 */
public class CryptoSupportPlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "cryptography-support-plugin";
    }

    @Override
    public Object nativeUnitModule() {
        return new CryptoSupportModule();
    }
}
