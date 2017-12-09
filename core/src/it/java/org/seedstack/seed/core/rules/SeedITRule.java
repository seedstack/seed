/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.rules;

import com.google.inject.Injector;
import io.nuun.kernel.api.Kernel;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.core.internal.CoreErrorCode;

/**
 * {@link Rule} to start/stop the {@link Kernel} and to inject a {@link BeforeKernel} method.
 */
public class SeedITRule implements TestRule {
    private static final Map<String, String> initialEnv = System.getenv();
    private final Object target;
    private final Object context;
    private Kernel kernel;

    public SeedITRule(Object target, Object context) {
        this.target = target;
        this.context = context;
    }

    public SeedITRule(Object target) {
        this.target = target;
        this.context = null;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        // Mock environment variables used to decode master password
        final Map<String, String> env = new HashMap<>(initialEnv);
        env.put("KS_PASSWD", "azerty");
        env.put("KEY_PASSWD", "azerty");

        Properties systemProperties = buildSystemProperties(description.getAnnotation(SystemProperties.class));
        systemProperties.setProperty("seedstack.config.config.watch", "false");
        new MockUp<System>() {
            @Mock
            public java.util.Map<String, String> getenv() {
                return env;
            }

            @Mock
            public Properties getProperties() {
                return systemProperties;
            }

            @Mock
            public String getProperty(String property) {
                return systemProperties.getProperty(property);
            }

            @Mock
            public String getProperty(String property, String def) {
                return systemProperties.getProperty(property, def);
            }
        };

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                for (Method method : target.getClass().getDeclaredMethods()) {
                    if (method.getAnnotation(BeforeKernel.class) != null) {
                        new FrameworkMethod(method).invokeExplosively(target);
                    }
                }

                startKernel();

                try {
                    base.evaluate();
                } catch (Exception e) {
                    throw SeedException.wrap(e, CoreErrorCode.UNEXPECTED_EXCEPTION);
                } finally {
                    stopKernel();
                }

            }

            private void startKernel() {
                kernel = Seed.createKernel(context, null, true);
                kernel.objectGraph().as(Injector.class).injectMembers(target);
            }

            private void stopKernel() {
                Seed.disposeKernel(kernel);
            }
        };
    }

    public Kernel getKernel() {
        return kernel;
    }

    private Properties buildSystemProperties(SystemProperties systemProperties) {
        Properties properties = System.getProperties();
        if (systemProperties != null) {
            String[] value = systemProperties.value();
            for (int i = 0; i < value.length; i += 2) {
                properties.setProperty(value[i], value[i + 1]);
            }
        }
        return properties;
    }
}
