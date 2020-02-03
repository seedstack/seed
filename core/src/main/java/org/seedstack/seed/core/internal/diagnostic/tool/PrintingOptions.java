/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.diagnostic.tool;

class PrintingOptions {
    private final boolean all;
    private final boolean missing;
    private final boolean file;

    PrintingOptions(boolean all, boolean missing, boolean file) {
        this.all = all;
        this.missing = missing;
        this.file = file;
    }

    boolean isAll() {
        return all;
    }

    boolean isMissing() {
        return missing;
    }

    boolean isFile() {
        return file;
    }
}
