/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.fixtures.data;

import java.util.ArrayList;
import java.util.List;
import org.seedstack.seed.DataImporter;
import org.seedstack.seed.DataSet;

@DataSet(group = "group1", name = "test1")
public class TestDataImporter implements DataImporter<TestDTO> {
    private static List<TestDTO> data = new ArrayList<>();

    private List<TestDTO> stagingArea = new ArrayList<>();

    public static List<TestDTO> getData() {
        return data;
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void importData(TestDTO data) {
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
}
