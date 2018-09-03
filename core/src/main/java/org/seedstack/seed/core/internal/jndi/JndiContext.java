/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.jndi;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import java.util.Hashtable;
import javax.inject.Inject;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * SeedStack JNDI context that can lookup for instances from the injector.
 */
class JndiContext implements Context {
    private static final String THIS_CONTEXT_IS_IMMUTABLE = "This context is immutable";
    @Inject
    private static Injector injector;
    private Hashtable<?, ?> environment;

    JndiContext(Hashtable<?, ?> environment) {
        this.environment = environment;
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        return this.lookup(name.toString());
    }

    @Override
    public Object lookup(String name) throws NamingException {
        if (injector == null) {
            throw new IllegalStateException("Missing SeedStack injector, cannot lookup for " + name);
        }

        try {
            int separatorIdx = name.indexOf('/');
            if (separatorIdx != -1) {
                String className = name.substring(0, separatorIdx);
                String qualifier = name.substring(separatorIdx + 1);
                return injector.getInstance(Key.get(Class.forName(className), Names.named(qualifier)));
            } else {
                return injector.getInstance(Class.forName(name));
            }
        } catch (ClassNotFoundException e) {
            NamingException exception = new NamingException("Unable to find " + name + " in SeedStack JNDI context");
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
        throw new NamingException(THIS_CONTEXT_IS_IMMUTABLE);
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
        throw new NamingException(THIS_CONTEXT_IS_IMMUTABLE);
    }

    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        throw new NamingException(THIS_CONTEXT_IS_IMMUTABLE);
    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {
        throw new NamingException(THIS_CONTEXT_IS_IMMUTABLE);
    }

    @Override
    public void unbind(Name name) throws NamingException {
        throw new NamingException(THIS_CONTEXT_IS_IMMUTABLE);
    }

    @Override
    public void unbind(String name) throws NamingException {
        throw new NamingException(THIS_CONTEXT_IS_IMMUTABLE);
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
        throw new NamingException(THIS_CONTEXT_IS_IMMUTABLE);
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        throw new NamingException(THIS_CONTEXT_IS_IMMUTABLE);
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException {
        throw new NamingException(THIS_CONTEXT_IS_IMMUTABLE);
    }

    @Override
    public void destroySubcontext(String name) throws NamingException {
        throw new NamingException(THIS_CONTEXT_IS_IMMUTABLE);
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        throw new NamingException(THIS_CONTEXT_IS_IMMUTABLE);
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        throw new NamingException(THIS_CONTEXT_IS_IMMUTABLE);
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object lookupLink(String name) throws NamingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public NameParser getNameParser(String name) throws NamingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String composeName(String name, String prefix) throws NamingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        throw new NamingException(THIS_CONTEXT_IS_IMMUTABLE);
    }

    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {
        throw new NamingException(THIS_CONTEXT_IS_IMMUTABLE);
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return this.environment;
    }

    @Override
    public void close() throws NamingException {
        // nothing to close on this context
    }

    @Override
    public String getNameInNamespace() throws NamingException {
        throw new UnsupportedOperationException();
    }
}
