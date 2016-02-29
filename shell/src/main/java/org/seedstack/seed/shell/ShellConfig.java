/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell;


import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;

@Config("shell")
public class ShellConfig {
    private static final int SHELL_DEFAULT_PORT = 2222;

    @SingleValue
    private boolean enabled;
    private int port = SHELL_DEFAULT_PORT;
    private KeyConfig key = new KeyConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public ShellConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ShellConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public KeyConfig key() {
        return key;
    }

    @Config("key")
    public static class KeyConfig {
        @SingleValue
        private KeyType type = KeyType.GENERATED;
        private String location;

        public KeyType getType() {
            return type;
        }

        public KeyConfig setType(KeyType type) {
            this.type = type;
            return this;
        }

        public String getLocation() {
            return location;
        }

        public KeyConfig setLocation(String location) {
            this.location = location;
            return this;
        }

        public enum KeyType {
            GENERATED,
            FILE,
            RESOURCE
        }
    }
}
