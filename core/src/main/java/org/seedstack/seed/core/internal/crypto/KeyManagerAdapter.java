/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.crypto;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.inject.Inject;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import org.seedstack.seed.Nullable;

class KeyManagerAdapter extends X509ExtendedKeyManager {
    private final X509KeyManager fallback;
    @Inject
    @Nullable
    private X509KeyManager delegate;

    KeyManagerAdapter(X509KeyManager fallback) {
        this.fallback = fallback;
    }

    @Override
    public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
        if (delegate instanceof X509ExtendedKeyManager) {
            return ((X509ExtendedKeyManager) delegate).chooseEngineClientAlias(keyType, issuers, engine);
        } else if (fallback instanceof X509ExtendedKeyManager) {
            return ((X509ExtendedKeyManager) fallback).chooseEngineClientAlias(keyType, issuers, engine);
        } else {
            return super.chooseEngineClientAlias(keyType, issuers, engine);
        }
    }

    @Override
    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        if (delegate instanceof X509ExtendedKeyManager) {
            return ((X509ExtendedKeyManager) delegate).chooseEngineServerAlias(keyType, issuers, engine);
        } else if (fallback instanceof X509ExtendedKeyManager) {
            return ((X509ExtendedKeyManager) fallback).chooseEngineServerAlias(keyType, issuers, engine);
        } else {
            return super.chooseEngineServerAlias(keyType, issuers, engine);
        }
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        if (delegate != null) {
            return delegate.getClientAliases(keyType, issuers);
        } else {
            return fallback.getClientAliases(keyType, issuers);
        }
    }

    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        if (delegate != null) {
            return delegate.chooseClientAlias(keyType, issuers, socket);
        } else {
            return fallback.chooseClientAlias(keyType, issuers, socket);
        }
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        if (delegate != null) {
            return delegate.getServerAliases(keyType, issuers);
        } else {
            return fallback.getServerAliases(keyType, issuers);
        }
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        if (delegate != null) {
            return delegate.chooseServerAlias(keyType, issuers, socket);
        } else {
            return fallback.chooseServerAlias(keyType, issuers, socket);
        }
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        if (delegate != null) {
            return delegate.getCertificateChain(alias);
        } else {
            return fallback.getCertificateChain(alias);
        }
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        if (delegate != null) {
            return delegate.getPrivateKey(alias);
        } else {
            return fallback.getPrivateKey(alias);
        }
    }
}
