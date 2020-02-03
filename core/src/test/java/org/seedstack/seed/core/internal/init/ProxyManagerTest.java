/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.ProxyConfig;

public class ProxyManagerTest {
    private ProxyManager underTest = ProxyManager.get();
    private ProxyConfig proxyConfig = new ProxyConfig();

    @Test
    public void testWithNoProxy() throws Exception {
        givenProxy(null, null, null, false);
        underTest.install(proxyConfig);
        List<Proxy> proxies = getProxySelector().select(new URI("http://app.domain.com:42"));
        underTest.uninstall();
        assertNoProxy(proxies);
    }

    @Test
    public void testWithProxy() throws Exception {
        givenProxy("HTTP", "proxy.mycompany.com", 8080, false);
        underTest.install(proxyConfig);
        List<Proxy> proxies = getProxySelector().select(new URI("http://app.otherdomain.com"));
        underTest.uninstall();
        assertProxy(proxies, Proxy.Type.HTTP, "proxy.mycompany.com", 8080);
    }

    @Test
    public void testWithUpperCaseProxy() throws Exception {
        givenProxy("HTTP", "proxy.mycompany.com", 8080, true);
        underTest.install(proxyConfig);
        List<Proxy> proxies = getProxySelector().select(new URI("http://app.otherdomain.com"));
        underTest.uninstall();
        assertProxy(proxies, Proxy.Type.HTTP, "proxy.mycompany.com", 8080);
    }

    @Test
    public void testProxyWithExclusion() throws Exception {
        testProxyWithExclusion("http://app.mycompany.com", "*.mycompany.com");
        testProxyWithExclusion("http://app.subdomain.mycompany.com", "*.mycompany.com");
        testProxyWithExclusion("http://app.mycompany.com", ".mycompany.com");
        testProxyWithExclusion("http://app.subdomain.mycompany.com", ".mycompany.com");
        testProxyWithExclusion("http://app.mycompany.com", "mycompany.com");
        testProxyWithExclusion("http://app.subdomain.mycompany.com", "mycompany.com");
        testProxyWithExclusion("http://app.subdomain.mycompany.com", "subdomain.mycompany.com");
    }

    @Test
    public void testProxyWithLocalhost() throws Exception {
        givenProxy("HTTP", "proxy.mycompany.com", 8080, false);
        underTest.install(proxyConfig);
        List<Proxy> proxies = getProxySelector().select(new URI("http://localhost"));
        underTest.uninstall();
        assertNoProxy(proxies);
    }

    @Test
    public void testProxyWithMultipleExclusion() throws Exception {
        givenProxy("HTTP", "proxy.mycompany.com", 8080, false, "*.mycompany.com", "*.otherdomain.com");
        underTest.install(proxyConfig);
        List<Proxy> proxies1 = getProxySelector().select(new URI("http://app.mycompany.com"));
        List<Proxy> proxies2 = getProxySelector().select(new URI("http://app.otherdomain.com"));
        List<Proxy> proxies3 = getProxySelector().select(new URI("http://app.yetanotherdomain.com"));
        underTest.uninstall();
        assertNoProxy(proxies1);
        assertNoProxy(proxies2);
        assertProxy(proxies3, Proxy.Type.HTTP, "proxy.mycompany.com", 8080);
    }

    @Test
    public void testProxyWithExclusionNoMatch() throws Exception {
        givenProxy("HTTP", "proxy.mycompany.com", 8080, false, "*.mycompany.com");
        underTest.install(proxyConfig);
        List<Proxy> proxies = getProxySelector().select(new URI("http://app.otherdomain.com"));
        underTest.uninstall();
        assertProxy(proxies, Proxy.Type.HTTP, "proxy.mycompany.com", 8080);
    }

    private void testProxyWithExclusion(String uri, String... exclusions) throws Exception {
        givenProxy("HTTP", "proxy.mycompany.com", 8080, false, exclusions);
        underTest.install(proxyConfig);
        List<Proxy> proxies = getProxySelector().select(new URI(uri));
        underTest.uninstall();
        assertNoProxy(proxies);
    }

    private void assertNoProxy(List<Proxy> proxies) {
        Assertions.assertThat(proxies).containsExactly(Proxy.NO_PROXY);
    }

    private void assertProxy(List<Proxy> proxies, Proxy.Type type, String host, int port) {
        Assertions.assertThat(proxies).hasSize(1);
        Assertions.assertThat(proxies.get(0).type()).isEqualTo(type);
        Assertions.assertThat(((InetSocketAddress) proxies.get(0).address()).getHostName()).isEqualTo(host);
        Assertions.assertThat(((InetSocketAddress) proxies.get(0).address()).getPort()).isEqualTo(port);
    }

    private ProxySelector getProxySelector() {
        return Deencapsulation.getField(underTest, "seedProxySelector");
    }

    private void givenProxy(String type, String host, Integer port, boolean upperCase, String... exclusions) {
        if (type != null) {
            new SystemProxyMockUp(upperCase, type, host, port, exclusions);
        } else {
            new SystemNoProxyMockUp();
        }
    }

    private static class SystemProxyMockUp extends MockUp<System> {
        private final boolean upperCase;
        private final String type;
        private final String host;
        private final Integer port;
        private final String[] exclusions;

        SystemProxyMockUp(boolean upperCase, String type, String host, Integer port, String... exclusions) {
            this.upperCase = upperCase;
            this.type = type;
            this.host = host;
            this.port = port;
            this.exclusions = exclusions;
        }

        @Mock
        String getenv(String name) {
            if (upperCase) {
                if (name.equals(type.toUpperCase() + "_PROXY"))
                    return String.format("http://%s:%d", host, port);
                if (name.equals(type.toLowerCase() + "_proxy"))
                    return null;
                if (name.equals("NO_PROXY"))
                    return String.join(",", (CharSequence[]) exclusions);
                if (name.equals("no_proxy"))
                    return null;
            } else {
                if (name.equals(type.toLowerCase() + "_proxy"))
                    return String.format("http://%s:%d", host, port);
                if (name.equals(type.toUpperCase() + "_PROXY"))
                    return null;
                if (name.equals("no_proxy"))
                    return String.join(",", (CharSequence[]) exclusions);
                if (name.equals("NO_PROXY"))
                    return null;
            }
            return null;
        }
    }

    private static class SystemNoProxyMockUp extends MockUp<System> {
        @Mock
        String getenv(String name) {
            return null;
        }
    }
}