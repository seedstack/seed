package org.seedstack.seed.core.internal.logging;

import org.seedstack.seed.core.internal.AbstractSeedPlugin;

public class LoggingPlugin extends AbstractSeedPlugin {
    @Override
    public String name() {
        return "logging";
    }

    @Override
    public Object nativeUnitModule() {
        return new LoggingModule();
    }
}
