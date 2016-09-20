/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration.tool;

import org.fusesource.jansi.Ansi;

import java.io.PrintStream;

class TreePrinter {
    private static final String INDENTATION = "  ";
    private final Node node;

    TreePrinter(Node node) {
        this.node = node;
    }

    void print(PrintStream printStream) {
        Ansi ansi = Ansi.ansi();
        printTree(node, "", ansi);
        printStream.println(ansi.toString());
    }

    private void printTree(Node node, String leftPadding, Ansi ansi) {
        if (!node.getName().isEmpty()) {
            ansi
                    .a(leftPadding)
                    .fg(Ansi.Color.YELLOW).a(node.getName()).reset()
                    .newline();
        }

        for (PropertyInfo propertyInfo : node.getPropertyInfo()) {
            ansi
                    .a(leftPadding).a(leftPadding)
                    .fgBright(Ansi.Color.CYAN).a(propertyInfo.isSingleValue() ? "+" : "").a(propertyInfo.getName()).reset()
                    .a("(")
                    .fgBright(Ansi.Color.MAGENTA).a(propertyInfo.getType()).reset()
                    .a(")")
                    .a(propertyInfo.getShortDescription())
                    .newline();
        }

        for (Node child : node.getChildren()) {
            printTree(child, leftPadding + INDENTATION, ansi);
        }
    }
}
