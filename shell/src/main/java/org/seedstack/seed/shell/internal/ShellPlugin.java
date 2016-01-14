/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell.internal;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.Factory;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.common.keyprovider.ResourceKeyPairProvider;
import org.apache.sshd.common.util.Buffer;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.seedstack.seed.Application;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This plugin provides shell-style interaction over SSH with the SEED application.
 *
 * @author adrien.lauer@mpsa.com
 */
public class ShellPlugin extends AbstractPlugin {
    private static final int SHELL_DEFAULT_PORT = 2222;
    private static final String SHELL_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.seed.shell";
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellPlugin.class);

    private int port;
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
    public InitState init(InitContext initContext) {
        Application application = initContext.dependency(ApplicationPlugin.class).getApplication();
        org.apache.commons.configuration.Configuration shellConfiguration = application.getConfiguration().subset(ShellPlugin.SHELL_PLUGIN_CONFIGURATION_PREFIX);

        // No need to go further if shell is not enabled
        if (!shellConfiguration.getBoolean("enabled", false)) {
            LOGGER.info("Shell support is present in the classpath but not enabled");
            return InitState.INITIALIZED;
        }

        port = shellConfiguration.getInt("port", SHELL_DEFAULT_PORT);
        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(port);

        String keyType = shellConfiguration.getString("key.type", "generated");
        if ("generated".equals(keyType)) {
            File storage = application.getStorageLocation("shell");
            sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(storage, "generate-key.ser").getAbsolutePath()));
        } else if ("file".equals(keyType)) {
            sshServer.setKeyPairProvider(new FileKeyPairProvider(new String[]{shellConfiguration.getString("key.location")}));
        } else if ("resource".equals(keyType)) {
            sshServer.setKeyPairProvider(new ResourceKeyPairProvider(new String[]{shellConfiguration.getString("key.location")}));
        }
        sshServer.setShellFactory(new Factory<Command>() {
            @Override
            public Command create() {
                return shellFactory.createInteractiveShell();
            }
        });

        sshServer.setCommandFactory(new CommandFactory() {
            @Override
            public Command createCommand(String command) {
                return shellFactory.createNonInteractiveShell(command);
            }
        });

        return InitState.INITIALIZED;
    }

    @Override
    public void start(Context context) {
        if (sshServer == null) {
            return;
        }

        List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<NamedFactory<UserAuth>>();
        userAuthFactories.add(new ShiroAuthFactory());
        sshServer.setUserAuthFactories(userAuthFactories);

        LOGGER.info("Starting SSH server on port " + this.port);
        try {
            sshServer.start();
        } catch (IOException e) {
            throw new PluginException("Unable to start SSH server on port " + this.port, e);
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
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(ApplicationPlugin.class);
    }

    private final class ShiroAuthFactory implements NamedFactory<UserAuth> {
        @Override
        public String getName() {
            return "password";
        }

        @Override
        public UserAuth create() {
            return new UserAuth() {
                @Override
                public Boolean auth(ServerSession session, String username, Buffer buffer) {
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
                }
            };
        }
    }

}
