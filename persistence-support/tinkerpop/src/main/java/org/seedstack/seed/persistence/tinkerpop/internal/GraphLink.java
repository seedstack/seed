/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.tinkerpop.internal;

import com.tinkerpop.blueprints.Graph;
import org.seedstack.seed.transaction.spi.TransactionalLink;

import java.util.ArrayDeque;
import java.util.Deque;

class GraphLink implements TransactionalLink<Graph> {
    private final Deque<Graph> graphDeque = new ArrayDeque<Graph>();

    @Override
    public Graph get() {
        Graph graph = this.graphDeque.peek();

        if (graph == null) {
            throw new IllegalStateException("Attempt to access entity manager outside a transaction");
        }

        return graph;
    }

    Graph getCurrentTransaction() {
        return graphDeque.peek();
    }

    void push(Graph graph) {
        graphDeque.push(graph);
    }

    Graph pop() {
        return graphDeque.pop();
    }
}
