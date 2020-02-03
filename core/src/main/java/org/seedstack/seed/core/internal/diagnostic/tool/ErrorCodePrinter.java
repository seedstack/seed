/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.diagnostic.tool;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import org.fusesource.jansi.Ansi;
import org.seedstack.shed.exception.ErrorCode;

class ErrorCodePrinter {
    private static final String INDENTATION = "  ";
    private final Class<? extends ErrorCode> errorCodeClass;
    private final boolean all;
    private final boolean missing;
    private final boolean file;

    ErrorCodePrinter(Class<? extends ErrorCode> errorCodeClass, PrintingOptions printingOptions) {
        this.errorCodeClass = errorCodeClass;
        this.all = printingOptions.isAll();
        this.missing = printingOptions.isMissing();
        this.file = printingOptions.isFile();
    }

    void print(PrintStream stream) {
        Ansi subAnsi = new Ansi();
        boolean someProperty = false;

        List<ErrorCode> errorCodes = new ArrayList<>(Arrays.asList(errorCodeClass.getEnumConstants()));
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

        Ansi ansi = new Ansi();
        if (someProperty) {
            ansi.fgBright(Ansi.Color.YELLOW)
                    .a(getTitle(errorCodeClass))
                    .reset()
                    .newline()
                    .a(subAnsi.toString())
                    .newline();
        }

        stream.print(ansi.toString());
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
        return result.substring(0, 1).toUpperCase(Locale.ENGLISH) + result.substring(1);
    }

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
