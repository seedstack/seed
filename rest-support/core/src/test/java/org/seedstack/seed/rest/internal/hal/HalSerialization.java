/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.hal;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.rest.api.hal.HalRepresentation;
import org.seedstack.seed.rest.internal.hal.fixture.RepresentationFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class HalSerialization {

    private static final String message = "{\"currentlyProcessing\":14,\"shippedToday\":20," +
            "\"_links\":{\"next\":{\"href\":\"/orders?page=2\"},\"self\":{\"href\":\"/orders\"},\"find\":{\"href\":\"/orders{?id}\",\"templated\":true}}," +
            "\"_embedded\":{\"orders\":[" +
              "{\"total\":30.0,\"currency\":\"USD\",\"status\":\"shipped\",\"_links\":{\"basket\":{\"href\":\"/baskets/98712\"},\"self\":{\"href\":\"/order/123\"},\"customer\":{\"href\":\"/customers/7809\"}}}," +
              "{\"total\":20.0,\"currency\":\"USD\",\"status\":\"processing\",\"_links\":{\"basket\":{\"href\":\"/baskets/97213\"},\"self\":{\"href\":\"/order/124\"},\"customer\":{\"href\":\"/customers/12369\"}}}]}}";

    @Test
    public void hal_body_writer() throws IOException {
        HalRepresentation halRep = new RepresentationFactory().createOrders();
        HalMessageBodyWriter halMessageBodyWriter = new HalMessageBodyWriter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        halMessageBodyWriter.writeTo(halRep, HalRepresentation.class, null, null, null, null, baos);

        Assertions.assertThat(message).isEqualTo(baos.toString());
    }
}
