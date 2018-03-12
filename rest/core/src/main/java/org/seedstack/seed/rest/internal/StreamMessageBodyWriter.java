/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
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
    @Context
    private Providers providers;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Stream.class.isAssignableFrom(type)
                && genericType instanceof ParameterizedType
                && ((ParameterizedType) genericType).getActualTypeArguments().length == 1;
    }

    @Override
    public void writeTo(Stream<T> tStream, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException, WebApplicationException {

        ParameterizedType iteratorType = newParameterizedType(
                Iterator.class,
                ((ParameterizedType) genericType).getActualTypeArguments()[0]
        );
        providers.getMessageBodyWriter(Iterator.class,
                iteratorType,
                annotations,
                mediaType)
                .writeTo(tStream.iterator(),
                        Iterator.class,
                        iteratorType,
                        annotations,
                        mediaType,
                        httpHeaders,
                        entityStream);
    }
}
