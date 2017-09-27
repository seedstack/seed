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
import org.seedstack.seed.DataSet;

@DataSet(group = "group1", name = "test1")
public class TestDataExporter implements DataExporter<TestDTO> {

    @Override
    public Iterator<TestDTO> exportData() {
        return Lists.newArrayList(new TestDTO("toto", "titi"), new TestDTO("machin", "truc")).iterator();
    }
}
