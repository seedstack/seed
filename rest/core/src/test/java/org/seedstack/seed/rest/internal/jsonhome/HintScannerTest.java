/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal.jsonhome;

import io.nuun.kernel.api.annotations.Ignore;
import java.lang.reflect.Method;
import java.util.Iterator;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.rest.Rel;

@Ignore
public class HintScannerTest {

    private HintScanner underTest;

    @Before
    public void before() {
        underTest = new HintScanner();
    }

    @Test
    public void testHints() {
        Hints hints = underTest.findHint(method(MyResource.class, "get"));
        Assertions.assertThat(hints.getAllow()).isNotEmpty();
        Assertions.assertThat(hints.getAllow().get(0)).isEqualTo("GET");

        Assertions.assertThat(hints.getFormats()).hasSize(3);
        Iterator<String> formatIterator = hints.getFormats().keySet().iterator();
        Assertions.assertThat(formatIterator.next()).isEqualTo("application/hal+json");
        Assertions.assertThat(formatIterator.next()).isEqualTo(MediaType.APPLICATION_JSON); // the map's keys are sorted
        Assertions.assertThat(formatIterator.next()).isEqualTo(MediaType.APPLICATION_XML);
    }

    private Method method(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            return clazz.getMethod(methodName, params);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Rel(value = "product", home = true)
    @Path("/product")
    static class MyResource {

        @GET
        @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
        @Produces({MediaType.APPLICATION_JSON, "application/hal+json"})
        public Response get() {
            return null;
        }
    }
}
