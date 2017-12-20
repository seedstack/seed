/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.configuration.tool;

import com.google.common.base.Strings;
import java.io.PrintStream;
import org.fusesource.jansi.Ansi;
import org.seedstack.shed.text.TextWrapper;

class DetailPrinter {
    private static final TextWrapper textWrapper = new TextWrapper(120);
    private final PropertyInfo propertyInfo;

    DetailPrinter(PropertyInfo propertyInfo) {
        this.propertyInfo = propertyInfo;
    }

    void printDetail(PrintStream stream) {
        Ansi ansi = Ansi.ansi();

        String title = "Details of " + propertyInfo.getName();
        ansi
                .a(title)
                .newline()
                .a(Strings.repeat("-", title.length()))
                .newline().newline();

        printSummary(propertyInfo, ansi);
        printDeclaration(propertyInfo, ansi);
        printLongDescription(propertyInfo, ansi);
        printAdditionalInfo(propertyInfo, ansi);
        ansi.newline();

        stream.print(ansi.toString());
    }

    private Ansi printSummary(PropertyInfo propertyInfo, Ansi ansi) {
        return ansi.a(propertyInfo.getShortDescription()).newline();
    }

    private void printDeclaration(PropertyInfo propertyInfo, Ansi ansi) {
        ansi
                .newline()
                .a("    ")
                .fgBright(Ansi.Color.MAGENTA)
                .a(propertyInfo.getType())
                .reset()
                .a(" ")
                .fgBright(Ansi.Color.BLUE)
                .a(propertyInfo.getName())
                .reset();

        Object defaultValue = propertyInfo.getDefaultValue();
        if (defaultValue != null) {
            ansi
                    .a(" = ")
                    .fgBright(Ansi.Color.GREEN)
                    .a(String.valueOf(defaultValue))
                    .reset();
        }

        ansi
                .a(";")
                .newline();
    }

    private void printLongDescription(PropertyInfo propertyInfo, Ansi ansi) {
        String longDescription = propertyInfo.getLongDescription();
        if (longDescription != null) {
            ansi.newline().a(textWrapper.wrap(longDescription)).newline();
        }
    }

    private void printAdditionalInfo(PropertyInfo propertyInfo, Ansi ansi) {
        if (propertyInfo.isMandatory() || propertyInfo.isSingleValue()) {
            ansi.newline();
        }
        if (propertyInfo.isMandatory()) {
            ansi.a("* This property is mandatory.").newline();
        }
        if (propertyInfo.isSingleValue()) {
            ansi.a("* This property is the default property of its declaring object").newline();
        }
    }
}
