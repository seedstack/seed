/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.security.shiro;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Iterator;

class SimpleFilterChain implements FilterChain {


    private final FilterChain originalChain;
    private final Iterator<? extends Filter> chain;

    private boolean originalCalled = false;

    public SimpleFilterChain(FilterChain originalChain, Iterator<? extends Filter> chain) {
        this.originalChain = originalChain;
        this.chain = chain;
    }

    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (chain.hasNext()) {
            Filter filter = chain.next();
            filter.doFilter(request, response, this);
        } else if (!originalCalled) {
            originalCalled = true;
            originalChain.doFilter(request, response);
        }
    }
}
