/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal;

import static com.google.inject.util.Types.newParameterizedType;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

@Provider
class StreamMessageBodyReader<T> implements MessageBodyReader<Stream<T>> {
    @Context
    private Providers providers;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Stream.class.isAssignableFrom(type)
                && genericType instanceof ParameterizedType
                && ((ParameterizedType) genericType).getActualTypeArguments().length == 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<T> readFrom(Class<Stream<T>> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream) throws WebApplicationException {

        ParameterizedType listType = newParameterizedType(
                List.class,
                ((ParameterizedType) genericType).getActualTypeArguments()[0]
        );

        try {
            return providers.getMessageBodyReader(List.class,
                    listType,
                    annotations,
                    mediaType)
                    .readFrom(List.class,
                            listType,
                            annotations,
                            mediaType,
                            httpHeaders,
                            entityStream).stream();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
