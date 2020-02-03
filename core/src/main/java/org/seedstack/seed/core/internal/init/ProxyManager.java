/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;

import static java.util.stream.Collectors.toList;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.seedstack.seed.ProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyManager.class);
    private SeedProxySelector seedProxySelector;
    private SeedProxyAuthenticator seedProxyAuthenticator;

    private ProxyManager() {
        // noop
    }

    public static ProxyManager get() {
        return Holder.INSTANCE;
    }

    public void install(ProxyConfig proxyConfig) {
        if (!proxyConfig.getMode().equals(ProxyConfig.ProxyMode.DISABLED)) {
            boolean autoProxy = proxyConfig.getMode().equals(ProxyConfig.ProxyMode.AUTO);
            String httpProxyValue = getValue(proxyConfig.getHttpProxy(), "http_proxy", autoProxy);
            String httpsProxyValue = getValue(proxyConfig.getHttpsProxy(), "https_proxy", autoProxy);
            String noProxyValue = getValue(proxyConfig.getNoProxy(), "no_proxy", autoProxy);

            Proxy httpProxy = buildProxy(httpProxyValue, 80);
            Proxy httpsProxy = buildProxy(httpsProxyValue, 443);
            List<Pattern> exclusions = buildExclusions(noProxyValue);

            ProxySelector.setDefault(seedProxySelector = new SeedProxySelector(
                    httpProxy,
                    httpsProxy,
                    ProxySelector.getDefault(),
                    exclusions
            ));

            PasswordAuthentication httpAuth = buildPasswordAuthentication(httpProxyValue);
            PasswordAuthentication httpsAuth = buildPasswordAuthentication(httpsProxyValue);
            Authenticator.setDefault(seedProxyAuthenticator = new SeedProxyAuthenticator(
                    httpAuth,
                    httpsAuth
            ));

            if (httpProxy == Proxy.NO_PROXY && httpsProxy == Proxy.NO_PROXY) {
                LOGGER.info("No proxy configured");
            } else if (Objects.equals(httpProxy, httpsProxy) && seedProxyAuthenticator.isHomogenous()) {
                logProxy("HTTP/HTTPS", httpProxy, httpAuth, noProxyValue);
            } else {
                logProxy("HTTP", httpProxy, httpAuth, noProxyValue);
                logProxy("HTTPS", httpsProxy, httpsAuth, noProxyValue);
            }
        }
    }

    private void logProxy(String protocol, Proxy proxy, PasswordAuthentication auth, String noProxyValue) {
        if (proxy != Proxy.NO_PROXY) {
            String authMessage = auth == null ?
                    "" :
                    " [" + auth.getUserName() + (auth.getPassword().length == 0 ? "" : ":***") + "]";
            if (Strings.isNullOrEmpty(noProxyValue)) {
                LOGGER.info("{} proxy configured to {}{} without exclusion",
                        protocol,
                        proxy.address().toString(),
                        authMessage);
            } else {
                LOGGER.info("{} proxy configured to {}{} excluding {}",
                        protocol,
                        proxy.address().toString(),
                        authMessage,
                        noProxyValue);
            }
        } else {
            LOGGER.info("No {} proxy configured", protocol);
        }
    }

    public void uninstall() {
        if (seedProxySelector != null) {
            ProxySelector.setDefault(null);
        }
        if (seedProxyAuthenticator != null) {
            Authenticator.setDefault(null);
        }
    }

    public void refresh(ProxyConfig proxyConfig) {
        uninstall();
        install(proxyConfig);
    }

    private String getValue(String configuredValue, String variable, boolean auto) {
        if (Strings.isNullOrEmpty(configuredValue) && auto) {
            String value = System.getenv(variable);
            if (Strings.isNullOrEmpty(value)) {
                value = System.getenv(variable.toUpperCase(Locale.ENGLISH));
            }
            return value;
        } else {
            return configuredValue;
        }
    }

    private List<Pattern> buildExclusions(String noProxy) {
        if (noProxy == null) {
            return Lists.newArrayList();
        }
        return Arrays.stream(noProxy.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::makePattern)
                .collect(toList());
    }

    private Proxy buildProxy(String value, int defaultPort) {
        return parseProxy(value, String.valueOf(defaultPort))
                .map(hostAndPort -> new Proxy(Proxy.Type.HTTP,
                        new InetSocketAddress(hostAndPort[0], Integer.parseInt(hostAndPort[1]))))
                .orElse(Proxy.NO_PROXY);
    }

    private PasswordAuthentication buildPasswordAuthentication(String value) {
        return parseCredentials(value)
                .map(credentials -> new PasswordAuthentication(credentials[0], credentials[1].toCharArray()))
                .orElse(null);
    }

    /**
     * Given a proxy URL returns a two element arrays containing the user name and the password. The second component
     * of the array is null if no password is specified.
     *
     * @param url The proxy host URL.
     * @return An optional containing an array of the user name and the password or empty when none are present or
     *         the url is empty.
     */
    private Optional<String[]> parseCredentials(String url) {
        if (!Strings.isNullOrEmpty(url)) {
            int p;
            if ((p = url.indexOf("://")) != -1) {
                url = url.substring(p + 3);
            }
            if ((p = url.indexOf('@')) != -1) {
                String[] result = new String[2];
                String credentials = url.substring(0, p);
                if ((p = credentials.indexOf(':')) != -1) {
                    result[0] = credentials.substring(0, p);
                    result[1] = credentials.substring(p + 1);
                } else {
                    result[0] = credentials;
                    result[1] = "";
                }
                return Optional.of(result);
            }
        }
        return Optional.empty();
    }

    /**
     * Given a proxy URL returns a two element arrays containing the host name and the port
     *
     * @param url     The proxy host URL.
     * @param defPort The default proxy port
     * @return An optional containing an array of the host name and the proxy port or empty when url is empty.
     */
    private Optional<String[]> parseProxy(String url, String defPort) {
        if (!Strings.isNullOrEmpty(url)) {
            String[] result = new String[2];
            int p = url.indexOf("://");
            if (p != -1)
                url = url.substring(p + 3);

            if ((p = url.indexOf('@')) != -1)
                url = url.substring(p + 1);

            if ((p = url.indexOf(':')) != -1) {
                result[0] = url.substring(0, p);
                result[1] = url.substring(p + 1);
            } else {
                result[0] = url;
                result[1] = defPort;
            }

            // remove trailing slash from the host name
            p = result[0].indexOf('/');
            if (p != -1) {
                result[0] = result[0].substring(0, p);
            }

            // remove trailing slash from the port number
            p = result[1].indexOf('/');
            if (p != -1) {
                result[1] = result[1].substring(0, p);
            }
            return Optional.of(result);
        }
        return Optional.empty();
    }

    /**
     * Creates a regexp pattern equivalent to the classic noProxy wildcard expression.
     *
     * @param noProxy the noProxy expression.
     * @return the regexp pattern.
     */
    private Pattern makePattern(String noProxy) {
        String regex = noProxy.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*");
        if (!regex.startsWith(".*")) {
            regex = ".*" + regex;
        }
        regex = "^" + regex + "$";
        return Pattern.compile(regex);
    }

    private static class Holder {
        private static final ProxyManager INSTANCE = new ProxyManager();
    }
}
