/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.scan.tomcat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Iterator;
import java.util.List;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import org.junit.Before;
import org.junit.Test;
import org.reflections.vfs.Vfs;

public class JndiInputDirTest {
    private URL goodURL;
    private URL badURL;

    private InputStream abInputStream = mock(InputStream.class);
    private InputStream aaaInputStream = mock(InputStream.class);

    @Before
    public void before() throws Exception {
        DirContext rootContext = mock(DirContext.class);
        DirContext aContext = mock(DirContext.class);
        DirContext bContext = mock(DirContext.class);
        DirContext aaContext = mock(DirContext.class);

        when(rootContext.list("/")).thenReturn(new ListNamingEnumeration<>(
                Lists.newArrayList(new NameClassPair("A", "dummy"), new NameClassPair("B", "dummy"))));
        when(rootContext.lookup("A")).thenReturn(aContext);
        when(rootContext.lookup("B")).thenReturn(bContext);

        when(aContext.list("/")).thenReturn(new ListNamingEnumeration<>(
                Lists.newArrayList(new NameClassPair("AA", "dummy"), new NameClassPair("AB", "dummy"))));
        when(aContext.lookup("AA")).thenReturn(aaContext);
        when(aContext.lookup("AB")).thenReturn(new ResourceMock(abInputStream));

        when(aaContext.list("/")).thenReturn(
                new ListNamingEnumeration<>(Lists.newArrayList(new NameClassPair("AAA", "dummy"))));
        when(aaContext.lookup("AAA")).thenReturn(new ResourceMock(aaaInputStream));

        when(bContext.list("/")).thenReturn(new ListNamingEnumeration<>(Lists.newArrayList()));

        final URLConnection goodUrlConnection = mock(URLConnection.class);
        when(goodUrlConnection.getContent()).thenReturn(rootContext);

        final URLConnection badUrlConnection = mock(URLConnection.class);
        when(badUrlConnection.getContent()).thenReturn(new Object());

        URLStreamHandler goodStubUrlHandler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) {
                return goodUrlConnection;
            }
        };

        URLStreamHandler badStubUrlHandler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) {
                return badUrlConnection;
            }
        };

        goodURL = new URL("foo", "bar", 99, "/foobar", goodStubUrlHandler);
        badURL = new URL("foo", "bar", 99, "/foobar", badStubUrlHandler);
    }

    @Test
    public void directory_traversal_is_working() throws Exception {
        JndiInputDir jndiInputDir = new JndiInputDir(goodURL);

        List<String> names = Lists.newArrayList();
        List<String> paths = Lists.newArrayList();

        for (Vfs.File file : jndiInputDir.getFiles()) {
            names.add(file.getName());
            paths.add(file.getRelativePath());

            if (file.getName().equals("AAA")) {
                assertThat(file.openInputStream()).isEqualTo(aaaInputStream);
            } else if (file.getName().equals("AB")) {
                assertThat(file.openInputStream()).isEqualTo(abInputStream);
            } else {
                fail("unexpected file " + file.getName());
            }
        }

        assertThat(names).isEqualTo(Lists.newArrayList("AAA", "AB"));
        assertThat(paths).isEqualTo(Lists.newArrayList("A/AA/AAA", "A/AB"));

        jndiInputDir.close();
    }

    @Test
    public void bad_context_is_ignored_instead_of_throwing_exception() {
        JndiInputDir jndiInputDir = new JndiInputDir(badURL);
        assertThat(jndiInputDir.getFiles()).isEmpty();
        jndiInputDir.close();
    }

    private class ListNamingEnumeration<T> implements NamingEnumeration<T> {
        private final Iterator<T> it;

        ListNamingEnumeration(List<T> list) {
            it = list.iterator();
        }

        @Override
        public T next() {
            return it.next();
        }

        @Override
        public boolean hasMore() {
            return it.hasNext();
        }

        @Override
        public void close() {

        }

        @Override
        public boolean hasMoreElements() {
            return it.hasNext();
        }

        @Override
        public T nextElement() {
            return it.next();
        }
    }

    private class ResourceMock {
        private final InputStream inputStream;

        private ResourceMock(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public InputStream streamContent() {
            return inputStream;
        }
    }
}
