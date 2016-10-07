/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import org.fusesource.jansi.Ansi;
import org.seedstack.seed.cli.CliOption;
import org.seedstack.seed.ErrorCode;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;

public class ErrorsTool extends AbstractSeedTool {
    private static final String INDENTATION = "  ";
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
        Ansi ansi = new Ansi();
        errorCodes.forEach((errorCodeClass) -> processErrorCodes(errorCodeClass, ansi));
        System.out.println(ansi.toString());
        return 0;
    }

    private void processErrorCodes(Class<? extends ErrorCode> errorCodeClass, Ansi ansi) {
        Ansi subAnsi = new Ansi();
        boolean someProperty = false;

        List<ErrorCode> errorCodes = new ArrayList<>();
        Arrays.stream(errorCodeClass.getEnumConstants()).forEach(errorCodes::add);
        Collections.sort(errorCodes, Comparator.comparing(Object::toString));

        for (ErrorCode errorCode : errorCodes) {
            String template = getTemplate(errorCode);
            if (all || !missing && !Strings.isNullOrEmpty(template) || missing && Strings.isNullOrEmpty(template)) {
                subAnsi
                        .a(INDENTATION)
                        .fgBright(Ansi.Color.BLUE)
                        .a(errorCode)
                        .reset()
                        .a("=")
                        .a(Optional.ofNullable(template).orElse(""))
                        .newline();
                someProperty = true;
            }
        }

        if (someProperty) {
            ansi.fgBright(Ansi.Color.YELLOW)
                    .a(getTitle(errorCodeClass))
                    .reset()
                    .newline()
                    .a(subAnsi.toString())
                    .newline();
        }
    }

    private String getTitle(Class<? extends ErrorCode> errorCodeClass) {
        if (file) {
            return errorCodeClass.getName().replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + ".properties";
        } else {
            return formatCamelCase(errorCodeClass.getSimpleName().replace("ErrorCodes", "").replace("ErrorCode", ""));
        }
    }

    private String formatCamelCase(String value) {
        String result = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, value).replace("_", " ");
        return result.substring(0, 1).toUpperCase() + result.substring(1);
    }

    @Nullable
    private String getTemplate(ErrorCode errorCode) {
        try {
            return ResourceBundle
                    .getBundle(errorCode.getClass().getName())
                    .getString(errorCode.toString());
        } catch (MissingResourceException e) {
            return null;
        }
    }
}
