/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.scan.tomcat;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.reflections.vfs.Vfs;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.CorePlugin;
import org.seedstack.seed.web.api.WebErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.AbstractIterator;

/**
 * VFS directory implementation for JNDI directory scanning.
 *
 * @author adrien.lauer@mpsa.com
 */
class JndiInputDir implements Vfs.Dir {
    private static final Logger LOGGER = LoggerFactory.getLogger(JndiInputDir.class);
    private final URL url;

    private List<String> fullPath = new ArrayList<String>();
    private Deque<DirContext> dirContextDeque = new ArrayDeque<DirContext>();
    private Deque<NamingEnumeration<NameClassPair>> enumerationDeque = new ArrayDeque<NamingEnumeration<NameClassPair>>();

    JndiInputDir(URL url) {
        this.url = url;
    }

    @Override
    public Iterable<Vfs.File> getFiles() {
        return new Iterable<Vfs.File>() {
            @Override
            public Iterator<Vfs.File> iterator() {
                return new AbstractIterator<Vfs.File>() {
                    { // NOSONAR
                        try {
                            Object content = url.openConnection().getContent();
                            dirContextDeque.push((DirContext) content);
                            enumerationDeque.push(((DirContext) content).list("/"));
                            fullPath.add(null);
                        } catch (Exception e) {
                            LOGGER.warn("Unable to open JNDI directory at " + url.toExternalForm() + ", ignoring it");
                            LOGGER.debug(CorePlugin.DETAILS_MESSAGE, e);
                        }
                    }

                    @Override
                    protected Vfs.File computeNext() {
                        try {
                            JndiInputFile nextFile = findNextFile();

                            if (nextFile == null) {
                                return endOfData();
                            } else {
                                return nextFile;
                            }
                        } catch (NamingException e) {
                            throw SeedException.wrap(e, WebErrorCode.UNABLE_TO_SCAN_TOMCAT_JNDI_DIRECTORY);
                        }
                    }
                };
            }
        };
    }

    @Override
    public void close() {
        for (DirContext dirContext : dirContextDeque) {
            try {
                dirContext.close();
            } catch (NamingException e) {
                LOGGER.warn("Unable to close JNDI directory at " + url.toExternalForm());
                LOGGER.debug(CorePlugin.DETAILS_MESSAGE, e);
            }
        }

        enumerationDeque.clear();
        dirContextDeque.clear();
        fullPath.clear();
    }

    @Override
    public String getPath() {
        return url.getPath();
    }

    private JndiInputFile findNextFile() throws NamingException {
        while (!enumerationDeque.isEmpty() && enumerationDeque.peek().hasMore()) {
            NameClassPair next = enumerationDeque.peek().next();
            Object entry = dirContextDeque.peek().lookup(next.getName());
            if (entry instanceof DirContext) {
                dirContextDeque.push((DirContext) entry);
                enumerationDeque.push(((DirContext) entry).list("/"));
                fullPath.add(next.getName());
            } else {
                return new JndiInputFile(entry, String.format("%s/%s", Joiner.on("/").skipNulls().join(fullPath), next.getName()));
            }
        }

        if (!enumerationDeque.isEmpty()) {
            enumerationDeque.pop();
            dirContextDeque.pop();
            fullPath.remove(fullPath.size() - 1);

            return findNextFile();
        } else {
            return null;
        }
    }
}
