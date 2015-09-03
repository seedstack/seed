/*
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal;

import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.core.NuunCore;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.web.api.WebErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class SeedServletServletExtension implements ServletExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeedServletServletExtension.class);

    @Override
    public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        try {
            Kernel kernel = createKernel(servletContext);
            kernel.init();
            kernel.start();

        } catch (SeedException e) {
            throw e;
        } catch (Exception e) {
            throw SeedException.wrap(e, WebErrorCode.UNEXPECTED_EXCEPTION);
        }
    }

    private static Kernel createKernel(ServletContext servletContext) {
        List<String> params = new ArrayList<String>();
        Enumeration<?> initparams = servletContext.getInitParameterNames();
        while (initparams.hasMoreElements()) {
            String keyName = (String) initparams.nextElement();
            if (keyName != null && !keyName.isEmpty()) {
                String value = servletContext.getInitParameter(keyName);
                LOGGER.debug("Setting kernel parameter {} to {}", keyName, value);
                params.add(keyName);
                params.add(value);
            }
        }

        return NuunCore.createKernel(NuunCore.newKernelConfiguration().containerContext(servletContext).params(params.toArray(new String[params.size()])));
    }
}
