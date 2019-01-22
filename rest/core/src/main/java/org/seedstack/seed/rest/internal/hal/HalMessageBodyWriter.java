/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.hal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.seedstack.seed.rest.hal.HalRepresentation;

@Provider
public class HalMessageBodyWriter implements MessageBodyWriter<HalRepresentation> {

    @Override
    public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return HalRepresentation.class.isAssignableFrom(
                type) && (mediaType == MediaType.APPLICATION_JSON_TYPE || mediaType.equals(
                new MediaType("application", "hal+json")));
    }

    @Override
    public long getSize(HalRepresentation halRepresentation, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType) {
        // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
        return -1;
    }

    @Override
    public void writeTo(HalRepresentation halRepresentation, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException, WebApplicationException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            entityStream.write(objectMapper.writeValueAsBytes(halRepresentation));
            entityStream.flush();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
