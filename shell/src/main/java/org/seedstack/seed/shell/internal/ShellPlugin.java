/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell.internal;

import com.google.inject.Module;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.common.keyprovider.ResourceKeyPairProvider;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.security.internal.SecurityGuiceConfigurer;
import org.seedstack.seed.security.internal.SecurityProvider;
import org.seedstack.seed.shell.ShellConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This plugin provides shell-style interaction over SSH with the SEED application.
 *
 * @author adrien.lauer@mpsa.com
 */
public class ShellPlugin extends AbstractSeedPlugin implements SecurityProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellPlugin.class);

    private ShellConfig shellConfig;
    private SshServer sshServer;

    @Inject
    private ShellFactory shellFactory;

    @Inject
    @Named("shell")
    private SecurityManager securityManager;

    @Override
    public String name() {
        return "shell";
    }

    @Override
    public InitState initialize(InitContext initContext) {
        shellConfig = getConfiguration(ShellConfig.class);

        // No need to go further if shell is not enabled
        if (!shellConfig.isEnabled()) {
            LOGGER.info("Shell support is present in the classpath but not enabled");
            return InitState.INITIALIZED;
        }

        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(shellConfig.getPort());

        ShellConfig.KeyConfig keyConfig = shellConfig.key();
        switch (keyConfig.getType()) {
            case GENERATED:
                File location;
                if (keyConfig.getLocation() == null) {
                    location = new File(getApplication().getStorageLocation("shell"), "generate-key.ser");
                } else {
                    location = new File(keyConfig.getLocation());
                }
                sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(location.getAbsolutePath()));
                break;
            case FILE:
                sshServer.setKeyPairProvider(new FileKeyPairProvider(new String[]{keyConfig.getLocation()}));
                break;
            case RESOURCE:
                sshServer.setKeyPairProvider(new ResourceKeyPairProvider(new String[]{keyConfig.getLocation()}));
                break;
        }

        sshServer.setShellFactory(() -> shellFactory.createInteractiveShell());
        sshServer.setCommandFactory(command -> shellFactory.createNonInteractiveShell(command));

        return InitState.INITIALIZED;
    }

    @Override
    public void start(Context context) {
        if (sshServer == null) {
            return;
        }

        List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<>();
        userAuthFactories.add(new ShiroAuthFactory());
        sshServer.setUserAuthFactories(userAuthFactories);

        LOGGER.info("Starting SSH server on port {}", shellConfig.getPort());
        try {
            sshServer.start();
        } catch (IOException e) {
            throw new PluginException("Unable to start SSH server on port " + shellConfig.getPort(), e);
        }
    }

    @Override
    public void stop() {
        if (sshServer == null) {
            return;
        }

        LOGGER.info("Stopping SSH server");
        try {
            sshServer.stop();
        } catch (InterruptedException e) {
            throw new PluginException("Unable to cleanly stop SSH server", e);
        }
    }

    @Override
    public Object nativeUnitModule() {
        return new ShellModule();
    }

    @Override
    public Module provideMainSecurityModule(SecurityGuiceConfigurer securityGuiceConfigurer) {
        return null;
    }

    @Override
    public Module provideAdditionalSecurityModule() {
        return new ShellSecurityModule();
    }

    private final class ShiroAuthFactory implements NamedFactory<UserAuth> {
        @Override
        public String getName() {
            return "password";
        }

        @Override
        public UserAuth create() {
            return (session, username, buffer) -> {
                boolean newPassword = buffer.getBoolean();
                if (newPassword) {
                    throw new IllegalStateException("password changes are not supported");
                }

                Subject subject = new Subject.Builder(securityManager).sessionId(session.getSessionId()).buildSubject();

                try {
                    subject.login(new UsernamePasswordToken(username, buffer.getString()));
                } catch (AuthenticationException e) {
                    LOGGER.warn("shell access denied to user " + username, e);
                    return false;
                }

                ThreadContext.bind(subject);
                ThreadContext.bind(securityManager);

                return true;
            };
        }
    }

}
