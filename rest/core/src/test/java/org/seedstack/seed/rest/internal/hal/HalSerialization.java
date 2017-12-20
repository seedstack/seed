/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal.hal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Test;
import org.seedstack.seed.rest.hal.HalRepresentation;
import org.seedstack.seed.rest.fixtures.RepresentationFactory;
import org.skyscreamer.jsonassert.JSONAssert;

public class HalSerialization {

    private static final String message = "{\"currentlyProcessing\":14,\"shippedToday\":20," +
            "\"_links\":{\"next\":{\"href\":\"/rest/orders?page=2\"},\"self\":{\"href\":\"/rest/orders\"},"
            + "\"find\":{\"href\":\"/rest/orders{?id}\",\"templated\":true}}," +
            "\"_embedded\":{\"orders\":[" +
            "{\"total\":30.0,\"currency\":\"USD\",\"status\":\"shipped\","
            + "\"_links\":{\"basket\":{\"href\":\"/rest/baskets/98712\"},\"self\":{\"href\":\"/rest/order/123\"},"
            + "\"customer\":{\"href\":\"/rest/customers/7809\"}}}," +
            "{\"total\":20.0,\"currency\":\"USD\",\"status\":\"processing\","
            + "\"_links\":{\"basket\":{\"href\":\"/rest/baskets/97213\"},\"self\":{\"href\":\"/rest/order/124\"},"
            + "\"customer\":{\"href\":\"/rest/customers/12369\"}}}]}}";

    @Test
    public void hal_body_writer() throws IOException, JSONException {
        HalRepresentation halRep = new RepresentationFactory().createOrders();
        HalMessageBodyWriter halMessageBodyWriter = new HalMessageBodyWriter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        halMessageBodyWriter.writeTo(halRep, HalRepresentation.class, null, null, null, null, baos);

        JSONAssert.assertEquals(message, baos.toString(), true);
    }
}
