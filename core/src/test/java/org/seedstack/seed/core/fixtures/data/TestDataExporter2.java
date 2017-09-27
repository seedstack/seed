/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.fixtures.data;

import com.google.common.collect.Lists;
import java.util.Iterator;
import org.seedstack.seed.DataExporter;

public class TestDataExporter2 implements DataExporter<TestDTO2> {
    @Override
    public Iterator<TestDTO2> exportData() {
        return Lists.newArrayList(new TestDTO2("toto2", "titi2", 12)).iterator();
    }
}
