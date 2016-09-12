/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto;

import org.seedstack.coffig.Config;
import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.SingleValue;
import org.seedstack.seed.crypto.spi.SSLAuthenticationMode;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Config("crypto")
public class CryptoConfig {
    private static final String MASTER_KEYSTORE_NAME = "master";
    private Map<String, KeyStoreConfig> keystores = new HashMap<>();
    private Map<String, CertificateConfig> certificates = new HashMap<>();
    private SSLConfig ssl = new SSLConfig();

    public Map<String, KeyStoreConfig> keyStores() {
        return Collections.unmodifiableMap(keystores);
    }

    public CryptoConfig addKeyStore(String name, KeyStoreConfig keyStoreConfig) {
        keystores.put(name, keyStoreConfig);
        return this;
    }

    public Map<String, CertificateConfig> certificates() {
        return Collections.unmodifiableMap(certificates);
    }

    public CryptoConfig addCertificate(String name, CertificateConfig certificateConfig) {
        certificates.put(name, certificateConfig);
        return this;
    }

    public KeyStoreConfig masterKeyStore() {
        return keystores.get(MASTER_KEYSTORE_NAME);
    }

    public SSLConfig ssl() {
        return ssl;
    }

    public static class KeyStoreConfig {
        private String path;
        private String password;
        private String type;
        private String provider;
        private Map<String, AliasConfig> aliases = new HashMap<>();

        public String getPath() {
            return path;
        }

        public KeyStoreConfig setPath(String path) {
            this.path = path;
            return this;
        }

        public String getPassword() {
            return password;
        }

        public KeyStoreConfig setPassword(String password) {
            this.password = password;
            return this;
        }

        public String getType() {
            return type;
        }

        public KeyStoreConfig setType(String type) {
            this.type = type;
            return this;
        }

        public String getProvider() {
            return provider;
        }

        public KeyStoreConfig setProvider(String provider) {
            this.provider = provider;
            return this;
        }

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
        private String file;

        public String getResource() {
            return resource;
        }

        public CertificateConfig setResource(String resource) {
            if (file != null) {
                throw new ConfigurationException("A certificate must be configured from either a resource or a file, not both");
            }
            this.resource = resource;
            return this;
        }

        public String getFile() {
            return file;
        }

        public CertificateConfig setFile(String file) {
            if (resource != null) {
                throw new ConfigurationException("A certificate must be configured from either a resource or a file, not both");
            }
            this.file = file;
            return this;
        }
    }

    /**
     * SSL configuration.
     */
    @Config("ssl")
    public static class SSLConfig {
        private String protocol = "TLS";
        private String keyStore = "default";
        private String trustStore = "default";
        private String alias = "ssl";
        private SSLAuthenticationMode clientAuthMode = SSLAuthenticationMode.NOT_REQUESTED;
        private Set<String> ciphers = new HashSet<>();

        /**
         * @return the requested protocol.
         */
        public String getProtocol() {
            return protocol;
        }

        /**
         * @return the key store name used for SSL (defaults to "default" if not specified).
         */
        public String getKeyStore() {
            return keyStore;
        }

        /**
         * @return the trust store name used for SSL (defaults to "default" if not specified).
         */
        public String getTrustStore() {
            return trustStore;
        }

        /**
         * @return the alias name used for SSL (defaults to "ssl" if not specified).
         */
        public String getAlias() {
            return alias;
        }

        /**
         * @return the client authentication mode (defaults to NOT_REQUESTED if not specified).
         */
        public SSLAuthenticationMode getClientAuthMode() {
            return clientAuthMode;
        }

        /**
         * @return the ciphers used.
         */
        public Set<String> getCiphers() {
            return ciphers;
        }
    }
}
