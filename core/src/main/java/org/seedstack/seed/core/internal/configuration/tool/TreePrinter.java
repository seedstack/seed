/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.configuration.tool;

import java.io.PrintStream;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiRenderer;

class TreePrinter {
    private static final String INDENTATION = "  ";
    private final Node node;

    TreePrinter(Node node) {
        this.node = node;
    }

    private static String defaultToString(Object o) {
        return o.getClass().getName() + "@" + Integer.toHexString(o.hashCode());
    }

    void printTree(PrintStream stream) {
        Ansi ansi = Ansi.ansi();

        ansi
                .a("Configuration options")
                .newline()
                .a("---------------------")
                .newline();

        printTree(node, "", ansi);

        ansi
                .newline()
                .a("(*) mandatory property")
                .newline()
                .a("(~) default property (can be specified as single value)")
                .newline();

        stream.print(ansi.toString());
    }

    private void printTree(Node node, String leftPadding, Ansi ansi) {
        if (!node.isRootNode()) {
            ansi
                    .a(leftPadding)
                    .fg(Ansi.Color.YELLOW).a(node.getName()).reset()
                    .newline();

            for (PropertyInfo propertyInfo : node.getPropertyInfo()) {
                printProperty(propertyInfo, leftPadding, ansi);
            }
        }

        for (Node child : node.getChildren()) {
            printTree(child, leftPadding + (node.isRootNode() ? "" : INDENTATION), ansi);
        }
    }

    private void printProperty(PropertyInfo propertyInfo, String leftPadding, Ansi ansi) {
        ansi
                .a(leftPadding)
                .a(INDENTATION)
                .fgBright(Ansi.Color.BLUE)
                .a(propertyInfo.isSingleValue() ? "~" : "")
                .a(propertyInfo.isMandatory() ? "*" : "")
                .a(propertyInfo.getName())
                .reset();

        Object defaultValue = propertyInfo.getDefaultValue();
        if (defaultValue != null) {
            String stringDefaultValue = String.valueOf(defaultValue);
            if (!defaultToString(defaultValue).equals(stringDefaultValue)) {
                ansi
                        .a(" = ")
                        .fgBright(Ansi.Color.GREEN)
                        .a(defaultValue instanceof String ? String.format("\"%s\"",
                                stringDefaultValue) : stringDefaultValue)
                        .reset();
            }
        }

        ansi
                .fgBright(Ansi.Color.MAGENTA)
                .a(" (")
                .a(propertyInfo.getType())
                .a(")")
                .reset()
                .a(": ")
                .a(AnsiRenderer.render(propertyInfo.getShortDescription()))
                .newline();

        for (PropertyInfo child : propertyInfo.getInnerPropertyInfo().values()) {
            printProperty(child, leftPadding + INDENTATION, ansi);
        }
    }
}
