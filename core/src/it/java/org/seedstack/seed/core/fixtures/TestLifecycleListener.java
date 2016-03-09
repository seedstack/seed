/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.fixtures;

import org.assertj.core.api.Assertions;
import org.seedstack.seed.LifecycleListener;
import org.seedstack.seed.Logging;
import org.slf4j.Logger;


public class TestLifecycleListener implements LifecycleListener {
    private static class State {
        private String token;
        private String startToken;
        private String stopToken;
    }

    @Logging
    private Logger logger;

    private static ThreadLocal<State> state = new ThreadLocal<State>() {
        @Override
        protected State initialValue() {
            return new State();
        }
    };

    @Override
    public void start() {
        Assertions.assertThat(logger).isNotNull();
        state.get().startToken = state.get().token;
    }

    @Override
    public void stop() {
        State state = TestLifecycleListener.state.get();
        state.stopToken = state.token;
    }

    public static void setToken(String refToken) {
        State state = TestLifecycleListener.state.get();
        state.token = refToken;
    }

    public static boolean isStartHasBeenCalled(String refToken) {
        State state = TestLifecycleListener.state.get();
        if (!refToken.equals(state.token)) {
            throw new IllegalStateException("Token is not valid");
        }
        return refToken.equals(state.startToken);
    }

    public static boolean isStopHasBeenCalled(String refToken) {
        State state = TestLifecycleListener.state.get();
        if (!refToken.equals(state.token)) {
            throw new IllegalStateException("Token is not valid");
        }
        return refToken.equals(state.stopToken);
    }
}
