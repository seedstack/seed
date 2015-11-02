/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.fixtures;

import org.seedstack.seed.spi.data.DataImporter;
import org.seedstack.seed.spi.data.DataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 12/03/14
 */
@DataSet(group="group2", name="test1")
public class TestDataImporter3 implements DataImporter<TestDTO2> {
    private static List<TestDTO2> data = new ArrayList<TestDTO2>();

    private List<TestDTO2> stagingArea = new ArrayList<TestDTO2>();

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public void importData(TestDTO2 data) {
        stagingArea.add(data);
    }

    @Override
    public void commit(boolean clear) {
        if (clear) {
            data.clear();
        }

        data.addAll(stagingArea);
        stagingArea.clear();
    }

    @Override
    public void rollback() {
        stagingArea.clear();
    }

    public static List<TestDTO2> getData() {
        return data;
    }
}
