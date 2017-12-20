/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.diagnostic.tool;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.seedstack.seed.cli.CliOption;
import org.seedstack.seed.core.internal.AbstractSeedTool;
import org.seedstack.shed.exception.ErrorCode;

public class ErrorsTool extends AbstractSeedTool {
    private List<Class<? extends ErrorCode>> errorCodes = new ArrayList<>();
    @CliOption(name = "m", longName = "missing")
    private boolean missing;
    @CliOption(name = "a", longName = "all")
    private boolean all;
    @CliOption(name = "f", longName = "file")
    private boolean file;

    @Override
    public String toolName() {
        return "errors";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .subtypeOf(ErrorCode.class)
                .build();
    }

    @Override
    protected InitState initialize(InitContext initContext) {
        initContext.scannedSubTypesByParentClass().get(ErrorCode.class)
                .stream()
                .filter(Enum.class::isAssignableFrom)
                .filter(ErrorCode.class::isAssignableFrom)
                .map((e) -> e.asSubclass(ErrorCode.class))
                .forEach(errorCodes::add);
        Collections.sort(errorCodes, Comparator.comparing(Class::getSimpleName));
        return InitState.INITIALIZED;
    }

    @Override
    public Integer call() throws Exception {
        PrintingOptions printingOptions = new PrintingOptions(all, missing, file);
        errorCodes.forEach((errorCodeClass) -> new ErrorCodePrinter(errorCodeClass, printingOptions).print(System.out));
        return 0;
    }
}
