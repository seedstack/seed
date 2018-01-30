/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.testing.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.seedstack.seed.testing.Arguments;
import org.seedstack.seed.testing.spi.TestContext;
import org.seedstack.seed.testing.spi.TestPlugin;
import org.seedstack.shed.reflect.Annotations;

public class ArgumentsTestPlugin implements TestPlugin {
    @Override
    public boolean enabled(TestContext testContext) {
        return true;
    }

    @Override
    public String[] arguments(TestContext testContext) {
        List<String> args = new ArrayList<>();

        // Arguments on the class
        Annotations.on(testContext.testClass())
                .includingMetaAnnotations()
                .find(Arguments.class)
                .ifPresent(arguments -> args.addAll(Arrays.asList(arguments.value())));

        // Arguments on the method
        testContext.testMethod().ifPresent(testMethod -> Annotations.on(testMethod)
                .includingMetaAnnotations()
                .find(Arguments.class)
                .ifPresent(arguments -> {
                    if (!arguments.append()) {
                        args.clear();
                    }
                    args.addAll(Arrays.asList(arguments.value()));
                }));

        return args.toArray(new String[args.size()]);
    }
}
