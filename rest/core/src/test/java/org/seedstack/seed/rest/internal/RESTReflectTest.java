/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal;

import java.lang.reflect.Method;
import java.util.Map;
import javax.ws.rs.BeanParam;
import javax.ws.rs.QueryParam;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class RESTReflectTest {

    public void httpMethod(String name, @QueryParam("foo") Integer aInt, @BeanParam Page page) {

    }

    @Test
    public void testFindQueryParam() throws NoSuchMethodException {
        Method httpMethod = RESTReflectTest.class.getMethod("httpMethod", String.class, Integer.class, Page.class);
        Map<String, String> queryParams = RESTReflect.findQueryParams("http://mycomp.com/params/", httpMethod);
        Assertions.assertThat(queryParams).containsOnlyKeys("foo", "pageIndex", "pageSize");
    }

    static class Page {
        @QueryParam("pageIndex")
        long pageIndex;
        @QueryParam("pageSize")
        long pageSize;
    }
}
