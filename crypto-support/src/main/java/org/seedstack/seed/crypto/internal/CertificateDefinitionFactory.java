/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 9 juin 2015
 */
/**
 * 
 */
package org.seedstack.seed.crypto.internal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.utils.SeedReflectionUtils;

/**
 * Factory to create a {@link CertificateDefinition}. This factory is checking the {@link X509Certificate} found.
 * 
 * @author thierry.bouvet@mpsa.com
 */
class CertificateDefinitionFactory {

    /**
     * Factory to create {@link CertificateDefinition}. This definition is used to create and check a {@link X509Certificate}.
     * 
     * @param configuration properties to define {@link CertificateDefinition}.
     * @return the {@link CertificateDefinition}.
     */
    public CertificateDefinition getInstance(Configuration configuration) {
        CertificateDefinition definition = new CertificateDefinition();
        String resource = configuration.getString("cert.resource");
        String certlocation = null;
        if (resource != null) {
            URL urlResource = SeedReflectionUtils.findMostCompleteClassLoader(null).getResource(resource);
            if (urlResource == null) {
                throw new RuntimeException("Certificate [" + resource + "] not found !");
            }
            certlocation = urlResource.getFile();
        } else {
            certlocation = configuration.getString("cert.file");
        }

        // Certificate informations
        if (certlocation != null) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(certlocation);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Certificate [" + certlocation + "] not found !");
            }
            try {
                definition.setCertificate(X509Certificate.getInstance(in));
            } catch (CertificateException e) {
                throw new RuntimeException("Certificate [" + certlocation + "] parsing error !");
            }
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException("Certificate [" + certlocation + "] not closed !");
            }
        }

        // Private key informations
        definition.setAlias(configuration.getString("keystore.alias"));
        definition.setPassword(configuration.getString("key.password"));

        return definition;
    }

}
