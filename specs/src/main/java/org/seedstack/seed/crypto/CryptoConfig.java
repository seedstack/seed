/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.crypto;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.X509KeyManager;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;
import org.seedstack.seed.crypto.spi.SSLAuthenticationMode;

@Config("crypto")
public class CryptoConfig {
    public static final String MASTER_KEY_STORE_NAME = "master";
    private StoreConfig truststore;
    private Map<String, KeyStoreConfig> keystores = new HashMap<>();
    private SSLConfig ssl = new SSLConfig();

    public StoreConfig getTrustStore() {
        return truststore;
    }

    public CryptoConfig setTrustStore(StoreConfig truststore) {
        this.truststore = truststore;
        return this;
    }

    public Map<String, KeyStoreConfig> keyStores() {
        return Collections.unmodifiableMap(keystores);
    }

    public CryptoConfig addKeyStore(String name, KeyStoreConfig keyStoreConfig) {
        keystores.put(name, keyStoreConfig);
        return this;
    }

    public SSLConfig ssl() {
        return ssl;
    }

    public static class StoreConfig {
        private String path;
        private String password;
        private String type;
        private String provider;

        public String getPath() {
            return path;
        }

        public StoreConfig setPath(String path) {
            this.path = path;
            return this;
        }

        public String getPassword() {
            return password;
        }

        public StoreConfig setPassword(String password) {
            this.password = password;
            return this;
        }

        public String getType() {
            return type;
        }

        public StoreConfig setType(String type) {
            this.type = type;
            return this;
        }

        public String getProvider() {
            return provider;
        }

        public StoreConfig setProvider(String provider) {
            this.provider = provider;
            return this;
        }
    }

    public static class KeyStoreConfig extends StoreConfig {
        private Map<String, AliasConfig> aliases = new HashMap<>();

        public KeyStoreConfig addAlias(String alias, AliasConfig aliasConfig) {
            aliases.put(alias, aliasConfig);
            return this;
        }

        public Map<String, AliasConfig> getAliases() {
            return Collections.unmodifiableMap(aliases);
        }

        public static class AliasConfig {
            @SingleValue
            private String password;
            private String qualifier;

            public String getPassword() {
                return password;
            }

            public AliasConfig setPassword(String password) {
                this.password = password;
                return this;
            }

            public String getQualifier() {
                return qualifier;
            }

            public AliasConfig setQualifier(String qualifier) {
                this.qualifier = qualifier;
                return this;
            }
        }
    }

    public static class CertificateConfig {
        private String resource;
        @SingleValue
        private String file;

        public String getResource() {
            return resource;
        }

        public CertificateConfig setResource(String resource) {
            if (file != null) {
                throw new IllegalStateException(
                        "A certificate must be configured from either a resource or a file, not both");
            }
            this.resource = resource;
            return this;
        }

        public String getFile() {
            return file;
        }

        public CertificateConfig setFile(String file) {
            if (resource != null) {
                throw new IllegalStateException(
                        "A certificate must be configured from either a resource or a file, not both");
            }
            this.file = file;
            return this;
        }
    }

    @Config("ssl")
    public static class SSLConfig {
        private static final String DEFAULT_PROTOCOL = "TLS";
        private String protocol = DEFAULT_PROTOCOL;
        private String keystore = MASTER_KEY_STORE_NAME;
        @SingleValue
        private String keyPassword;
        private SSLAuthenticationMode clientAuthMode = SSLAuthenticationMode.NOT_REQUESTED;
        private Set<String> ciphers = new HashSet<>();
        private Class<? extends X509KeyManager> x509KeyManager;
        private String keyManagerAlgorithm;
        private String trustManagerAlgorithm;
        private String randomAlgorithm;

        public String getProtocol() {
            return protocol;
        }

        public SSLConfig setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public String getKeystore() {
            return keystore;
        }

        public SSLConfig setKeystore(String keystore) {
            this.keystore = keystore;
            return this;
        }

        public String getKeyPassword() {
            return keyPassword;
        }

        public void setKeyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
        }

        public SSLAuthenticationMode getClientAuthMode() {
            return clientAuthMode;
        }

        public SSLConfig setClientAuthMode(SSLAuthenticationMode clientAuthMode) {
            this.clientAuthMode = clientAuthMode;
            return this;
        }

        public Set<String> getCiphers() {
            return Collections.unmodifiableSet(ciphers);
        }

        public SSLConfig addCipher(String cipher) {
            this.ciphers.add(cipher);
            return this;
        }

        public Class<? extends X509KeyManager> getX509KeyManager() {
            return x509KeyManager;
        }

        public SSLConfig setX509KeyManager(Class<? extends X509KeyManager> x509KeyManager) {
            this.x509KeyManager = x509KeyManager;
            return this;
        }

        public String getKeyManagerAlgorithm() {
            return keyManagerAlgorithm;
        }

        public SSLConfig setKeyManagerAlgorithm(String keyManagerAlgorithm) {
            this.keyManagerAlgorithm = keyManagerAlgorithm;
            return this;
        }

        public String getTrustManagerAlgorithm() {
            return trustManagerAlgorithm;
        }

        public SSLConfig setTrustManagerAlgorithm(String trustManagerAlgorithm) {
            this.trustManagerAlgorithm = trustManagerAlgorithm;
            return this;
        }

        public String getRandomAlgorithm() {
            return randomAlgorithm;
        }

        public SSLConfig setRandomAlgorithm(String randomAlgorithm) {
            this.randomAlgorithm = randomAlgorithm;
            return this;
        }
    }
}
