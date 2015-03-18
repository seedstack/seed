/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cache;

import org.seedstack.seed.it.api.ITBind;

import javax.cache.Cache;
import javax.cache.annotation.CacheResult;
import javax.inject.Inject;
import javax.inject.Named;

@ITBind
public class CacheSample {
    private boolean alreadyInvoked1 = false;
    private boolean alreadyInvoked2 = false;

    @Inject @Named("testcache1")
    private Cache cache1;

    @Inject @Named("testcache2")
    private Cache cache2;

    @CacheResult(cacheName = "testcache1")
    public int add1(int a, int b) {
        if (alreadyInvoked1)
            throw new IllegalStateException("cache is called a second time !");
        alreadyInvoked1 = true;

        return a + b;
    }

    @CacheResult(cacheName = "testcache2")
    public int add2(int a, int b) {
        if (alreadyInvoked2)
            throw new IllegalStateException("cache is called a second time !");
        alreadyInvoked2 = true;

        return a + b;
    }

    public Cache getCache1() {
        return cache1;
    }

    public Cache getCache2() {
        return cache2;
    }
}
