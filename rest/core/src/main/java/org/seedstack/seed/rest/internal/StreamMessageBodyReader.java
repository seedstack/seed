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
    private static final ParameterizedType GENERIC_TYPE = newParameterizedType(
            List.class,
            Object.class
    );

    @Context
    private Providers providers;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Stream.class.isAssignableFrom(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<T> readFrom(Class<Stream<T>> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream) throws IOException, WebApplicationException {

        // If we don't use an intermediate list, the underlying stream gets closed before we
        // get the chance to read the stream. Maybe this can be fixed/improved.

        return providers.getMessageBodyReader(List.class,
                GENERIC_TYPE,
                annotations,
                mediaType)
                .readFrom(List.class,
                        GENERIC_TYPE,
                        annotations,
                        mediaType,
                        httpHeaders,
                        entityStream).stream();
    }
}
