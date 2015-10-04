/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.data;

import java.util.Iterator;

/**
 * Marker of a data set beginning in a exported stream.
 *
 * @author adrien.lauer@mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 */
class DataSetMarker<T> {
    private String group;
    private String name;
    private Iterator<T> items;

    DataSetMarker() {
    }

    DataSetMarker(String group, String name, Iterator<T> items) {
        this.group = group;
        this.name = name;
        this.items = items;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Iterator<T> getItems() {
        return items;
    }

    public void setItems(Iterator<T> items) {
        this.items = items;
    }
}
