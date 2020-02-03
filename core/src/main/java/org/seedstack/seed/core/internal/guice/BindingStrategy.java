/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.guice;

import com.google.inject.Binder;

/**
 * The BindingStrategy interface deports strategies to resolve bindings.
 */
public interface BindingStrategy {

    /**
     * Resolves the bindings for the given strategy using the current module binder.
     *
     * @param binder the current Binder
     */
    void resolve(Binder binder);
}
