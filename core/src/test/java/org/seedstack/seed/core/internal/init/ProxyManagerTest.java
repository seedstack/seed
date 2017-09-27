/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
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
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.ProxyConfig;

@RunWith(JMockit.class)
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
        givenProxy("HTTP", "proxy.mycompany.com", 8080, false, "*.mycompany.com");
        underTest.install(proxyConfig);
        List<Proxy> proxies = getProxySelector().select(new URI("http://app.mycompany.com"));
        underTest.uninstall();
        assertNoProxy(proxies);
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

    private void assertNoProxy(List<Proxy> proxies) {
        Assertions.assertThat(proxies).containsExactly(Proxy.NO_PROXY);
    }

    private void assertProxy(List<Proxy> proxies, Proxy.Type type, String host, int port) {
        Assertions.assertThat(proxies).hasSize(1);
        Assertions.assertThat(proxies.get(0).type()).isEqualTo(type);
        Assertions.assertThat(((InetSocketAddress) proxies.get(0).address()).getHostName()).isEqualTo(host);
        Assertions.assertThat(((InetSocketAddress) proxies.get(0).address()).getPort()).isEqualTo(port);
    }

    private void givenProxy(String type, String host, Integer port, boolean upperCase, String... exclusions) {
        if (type != null) {
            new NonStrictExpectations(System.class) {{
                System.getenv(upperCase ? (type.toUpperCase() + "_PROXY") : (type.toLowerCase() + "_proxy"));
                result = String.format("http://%s:%d", host, port);
                System.getenv(!upperCase ? (type.toUpperCase() + "_PROXY") : (type.toLowerCase() + "_proxy"));
                result = null;

                System.getenv(upperCase ? "NO_PROXY" : "no_proxy");
                result = String.join(",", (CharSequence[]) exclusions);
                System.getenv(!upperCase ? "NO_PROXY" : "no_proxy");
                result = null;
            }};
        } else {
            new NonStrictExpectations(System.class) {{
                System.getenv("http_proxy");
                result = null;
                System.getenv("HTTP_PROXY");
                result = null;
                System.getenv("https_proxy");
                result = null;
                System.getenv("HTTPS_PROXY");
                result = null;
                System.getenv("no_proxy");
                result = null;
                System.getenv("NO_PROXY");
                result = null;
            }};
        }
    }

    private ProxySelector getProxySelector() {
        return Deencapsulation.getField(underTest, "seedProxySelector");
    }
}