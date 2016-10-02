package org.seedstack.seed.core.internal.logging;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

class LoggingModule extends AbstractModule {
    @Override
    protected void configure() {
        bindListener(Matchers.any(), new LoggingTypeListener());
    }
}
