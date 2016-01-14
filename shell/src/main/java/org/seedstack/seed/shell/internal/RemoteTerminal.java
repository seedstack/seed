/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell.internal;

import jline.TerminalSupport;

/**
 * This terminal is an implementation of TerminalSupport. It is near than UnsupportedTerminal but it also supports ansi.
 *
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 19/06/2014
 */
class RemoteTerminal extends TerminalSupport {

    RemoteTerminal(boolean supported) {
        super(supported);
        setAnsiSupported(true);
        setEchoEnabled(true);
    }
}
