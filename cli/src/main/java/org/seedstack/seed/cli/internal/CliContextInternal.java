/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli.internal;

import static org.assertj.core.util.Preconditions.checkNotNull;

class CliContextInternal implements org.seedstack.seed.cli.CliContext {
    private final String[] args;

    CliContextInternal(String[] args) {
        this.args = checkNotNull(args).clone();
    }

    public String[] getArgs() {
        return args.clone();
    }
}
