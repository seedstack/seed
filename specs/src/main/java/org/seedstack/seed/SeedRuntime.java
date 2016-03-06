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

    public SeedRuntime(Object context, DiagnosticManager diagnosticManager, boolean colorOutputSupported) {
        this.context = context;
        this.diagnosticManager = diagnosticManager;
        this.colorOutputSupported = colorOutputSupported;
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
}
