/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.fixtures;

import com.google.common.collect.Lists;
import org.seedstack.seed.core.spi.data.DataExporter;

import java.util.Iterator;

public class TestDataExporter2 implements DataExporter<TestDTO2> {
    @Override
    public Iterator<TestDTO2> exportData() {
        return Lists.newArrayList(new TestDTO2("toto2", "titi2", 12)).iterator();
    }
}
