/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import com.google.common.collect.Lists;
import org.seedstack.seed.core.api.CoreErrorCode;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.CorePlugin;
import org.seedstack.seed.core.spi.SeedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ServiceLoader;

public class SeedRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeedRunner.class);

    public static void main(String[] args) {
        List<SeedRunnable> entryPointServices = Lists.newArrayList(ServiceLoader.load(SeedRunnable.class));
        int returnCode = 0;

        if (entryPointServices.size() < 1) {
            throw SeedException.createNew(CoreErrorCode.MISSING_SEED_ENTRY_POINT);
        } else if (entryPointServices.size() > 1) {
            throw SeedException.createNew(CoreErrorCode.MULTIPLE_SEED_ENTRY_POINTS);
        }

        try {
            returnCode = entryPointServices.get(0).run(args);
        } catch (SeedException e) {
            handleException(e);
            e.printStackTrace(System.err);
        } catch (Exception e) {
            handleException(e);
            SeedException.wrap(e, CoreErrorCode.UNEXPECTED_EXCEPTION).printStackTrace(System.err);
        }

        // no java.lang.Error handling is done

        System.exit(returnCode);
    }

    private static void handleException(Exception e) {
        LOGGER.error("An exception occurred during CLI application startup, collecting diagnostic information");
        CorePlugin.getDiagnosticManager().dumpDiagnosticReport(e);
    }
}
