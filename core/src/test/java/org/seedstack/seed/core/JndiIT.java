/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;

import com.google.inject.Injector;
import java.util.Enumeration;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Bind;
import org.seedstack.seed.JndiContext;
import org.seedstack.seed.core.fixtures.Service1;
import org.seedstack.seed.testing.junit4.SeedITRunner;

@RunWith(SeedITRunner.class)
public class JndiIT {
    @Inject
    private Injector injector;

    @Test
    public void jndi_context_injection_is_working() {
        Holder holder = injector.getInstance(Holder.class);
        Assertions.assertThat(holder.defaultCtx).isNotNull();
        Assertions.assertThat(holder.defaultCtxViaName).isNotNull();
        Assertions.assertThat(holder.ctx1).isNotNull();
        Assertions.assertThat(holder.ctx2).isNotNull();
        Assertions.assertThat(holder.defaultCtx).isSameAs(holder.defaultCtxViaName);
    }

    @Test
    public void jndi_context_lookup_is_working() throws NamingException {
        Service1 service1 = (Service1) injector.getInstance(Holder.class).ctx1.lookup(
                "org.seedstack.seed.core.fixtures.Service1");
        Assertions.assertThat(service1).isNotNull();
    }

    @Test(expected = NamingException.class)
    public void wrong_class_name_lookup_throws_exception() throws NamingException {
        injector.getInstance(Holder.class).ctx1.lookup("org.seedstack.seed.core.fixtures.WrongName");
        fail("should have failed");
    }

    @Test
    public void jndi_context_named_lookup_is_working() throws NamingException {
        Service1 service1ByClassAndName = (Service1) injector.getInstance(Holder.class).ctx1.lookup(
                "org.seedstack.seed.core.fixtures.Service/Service1");
        Service1 service1ByClass = (Service1) injector.getInstance(Holder.class).ctx1.lookup(
                "org.seedstack.seed.core.fixtures.Service1");

        Assertions.assertThat(service1ByClassAndName).isNotNull();
        Assertions.assertThat(service1ByClassAndName).isSameAs(service1ByClass);
    }

    @Test
    public void two_identically_configured_jndi_contexts_are_not_the_same_instance() {
        Holder holder = injector.getInstance(Holder.class);
        Assertions.assertThat(holder.ctx1).isNotSameAs(holder.ctx2);
    }

    @Test
    public void explicit_resource_injection_from_default_context_is_working() throws NamingException {
        Holder holder = injector.getInstance(Holder.class);
        Assertions.assertThat(holder.service1Default).isNotNull();
        Assertions.assertThat(holder.service1Named).isEqualTo(
                injector.getInstance(Holder.class).ctx1.lookup("org.seedstack.seed.core.fixtures.Service1"));
    }

    @Test
    public void explicit_resource_injection_from_named_context_is_working() throws NamingException {
        Holder holder = injector.getInstance(Holder.class);
        Assertions.assertThat(holder.service1Named).isNotNull();
        Assertions.assertThat(holder.service1Named).isEqualTo(
                injector.getInstance(Holder.class).ctx1.lookup("org.seedstack.seed.core.fixtures.Service1"));
    }

    @Test
    public void implicit_resource_injection_is_working() throws NamingException {
        Holder holder = injector.getInstance(Holder.class);
        Assertions.assertThat(holder.service1Default).isNotNull();
        Assertions.assertThat(holder.service1Named).isEqualTo(
                injector.getInstance(Holder.class).ctx1.lookup("org.seedstack.seed.core.fixtures.Service1"));
    }

    @Test
    public void jndi_context_is_immutable() {
        Context context = injector.getInstance(Holder.class).ctx1;
        assertThatExceptionOfType(NamingException.class).isThrownBy(() -> context.bind(new DummyName(), new Object()));
        assertThatExceptionOfType(NamingException.class).isThrownBy(() -> context.bind("", new Object()));
        assertThatExceptionOfType(NamingException.class).isThrownBy(
                () -> context.rebind(new DummyName(), new Object()));
        assertThatExceptionOfType(NamingException.class).isThrownBy(() -> context.rebind("", new Object()));
        assertThatExceptionOfType(NamingException.class).isThrownBy(() -> context.unbind(new DummyName()));
        assertThatExceptionOfType(NamingException.class).isThrownBy(() -> context.unbind(""));
        assertThatExceptionOfType(NamingException.class).isThrownBy(
                () -> context.rename(new DummyName(), new DummyName()));
        assertThatExceptionOfType(NamingException.class).isThrownBy(() -> context.rename("", ""));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> context.list(new DummyName()));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> context.list(""));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(
                () -> context.listBindings(new DummyName()));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> context.listBindings(""));
        assertThatExceptionOfType(NamingException.class).isThrownBy(() -> context.destroySubcontext(new DummyName()));
        assertThatExceptionOfType(NamingException.class).isThrownBy(() -> context.destroySubcontext(""));
        assertThatExceptionOfType(NamingException.class).isThrownBy(() -> context.createSubcontext(new DummyName()));
        assertThatExceptionOfType(NamingException.class).isThrownBy(() -> context.createSubcontext(""));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(
                () -> context.lookupLink(new DummyName()));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> context.lookupLink(""));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(
                () -> context.getNameParser(new DummyName()));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> context.getNameParser(""));
        // Cannot test composeName methods (implemented in InitialContext)
        assertThatExceptionOfType(NamingException.class).isThrownBy(() -> context.addToEnvironment("", new Object()));
        assertThatExceptionOfType(NamingException.class).isThrownBy(() -> context.removeFromEnvironment(""));
        assertThatExceptionOfType(NamingException.class).isThrownBy(() -> context.removeFromEnvironment(""));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(context::getNameInNamespace);
    }

    @Bind
    static class Holder {
        @Inject
        Context defaultCtx;

        @Inject
        @Named("defaultContext")
        Context defaultCtxViaName;

        @Inject
        @Named("test1")
        Context ctx1;

        @Inject
        @Named("test2")
        Context ctx2;

        @Resource(name = "org.seedstack.seed.core.fixtures.Service/Service1")
        Service1 service1Default;

        @Resource(name = "org.seedstack.seed.core.fixtures.Service1")
        @JndiContext("test2")
        Service1 service1Named;
    }

    private static class DummyName implements Name {
        @Override
        public int compareTo(Object obj) {
            return 0;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Enumeration<String> getAll() {
            return null;
        }

        @Override
        public String get(int posn) {
            return null;
        }

        @Override
        public Name getPrefix(int posn) {
            return null;
        }

        @Override
        public Name getSuffix(int posn) {
            return null;
        }

        @Override
        public boolean startsWith(Name n) {
            return false;
        }

        @Override
        public boolean endsWith(Name n) {
            return false;
        }

        @Override
        public Name addAll(Name suffix) {
            return null;
        }

        @Override
        public Name addAll(int posn, Name n) {
            return null;
        }

        @Override
        public Name add(String comp) {
            return null;
        }

        @Override
        public Name add(int posn, String comp) {
            return null;
        }

        @Override
        public Object remove(int posn) {
            return null;
        }

        @Override
        public Object clone() {
            return null;
        }
    }
}
