/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import static com.google.inject.util.Types.newParameterizedType;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.stream.Stream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

@Provider
class StreamMessageBodyWriter<T> implements MessageBodyWriter<Stream<T>> {
    private static final ParameterizedType GENERIC_TYPE = newParameterizedType(
            Iterator.class,
            Object.class
    );

    @Context
    private Providers providers;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Stream.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Stream<T> tStream, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1L;
    }

    @Override
    public void writeTo(Stream<T> tStream, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException, WebApplicationException {

        providers.getMessageBodyWriter(Iterator.class,
                GENERIC_TYPE,
                annotations,
                mediaType)
                .writeTo(tStream.iterator(),
                        Iterator.class,
                        GENERIC_TYPE,
                        annotations,
                        mediaType,
                        httpHeaders,
                        entityStream);
    }
}
