/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

public class SeedRuntime {
    private final Object context;
    private final DiagnosticManager diagnosticManager;
    private final boolean colorOutputSupported;
    private final String seedVersion;

    private SeedRuntime(Object context, DiagnosticManager diagnosticManager, boolean colorOutputSupported, String seedVersion) {
        this.context = context;
        this.diagnosticManager = diagnosticManager;
        this.colorOutputSupported = colorOutputSupported;
        this.seedVersion = seedVersion;
    }

    public <T> T contextAs(Class<T> tClass) {
        if (context != null && tClass.isAssignableFrom(context.getClass())) {
            return tClass.cast(context);
        } else {
            return null;
        }
    }

    public DiagnosticManager getDiagnosticManager() {
        return diagnosticManager;
    }

    public boolean isColorOutputSupported() {
        return colorOutputSupported;
    }

    public String getVersion() {
        return seedVersion;
    }

    public static class Builder {
        private Object _context;
        private DiagnosticManager _diagnosticManager;
        private boolean _colorSupported;
        private String _version;

        private Builder() {
        }

        public Builder context(Object context) {
            this._context = context;
            return this;
        }

        public Builder diagnosticManager(DiagnosticManager diagnosticManager) {
            this._diagnosticManager = diagnosticManager;
            return this;
        }

        public Builder colorSupported(boolean colorSupported) {
            this._colorSupported = colorSupported;
            return this;
        }

        public Builder version(String version) {
            this._version = version;
            return this;
        }

        public SeedRuntime build() {
            return new SeedRuntime(_context, _diagnosticManager, _colorSupported, _version);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
