/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.testing.junit4.internal;

import static org.seedstack.shed.misc.PriorityUtils.sortByPriority;

import com.google.inject.Injector;
import io.nuun.kernel.api.Kernel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.spi.SeedLauncher;
import org.seedstack.seed.testing.LaunchMode;
import org.seedstack.seed.testing.spi.TestDecorator;
import org.seedstack.seed.testing.spi.TestPlugin;
import org.seedstack.shed.ClassLoaders;
import org.seedstack.shed.exception.Throwing;
import org.seedstack.shed.misc.PriorityUtils;
import org.seedstack.shed.reflect.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JUnit4Runner extends BlockJUnit4ClassRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(JUnit4Runner.class);
    private static final String CONFIG_PREFIX = "seedstack.config.";
    private static final Class<? extends SeedLauncher> DEFAULT_IT_LAUNCHER_CLASS = Classes
            .optional("org.seedstack.seed.core.internal.it.DefaultITLauncher")
            .filter(SeedLauncher.class::isAssignableFrom)
            .map(c -> (Class<? extends SeedLauncher>) c.asSubclass(SeedLauncher.class))
            .orElse(null);
    private final JUnit4TestContext testContext;
    private final List<TestPlugin> plugins = new ArrayList<>();
    private final List<Class<? extends TestDecorator>> decorators = new ArrayList<>();
    private final LaunchMode launchMode;
    private SeedLauncher seedLauncher;

    public JUnit4Runner(Class<?> someClass) throws InitializationError {
        super(someClass);

        // Create test context
        testContext = new JUnit4TestContext(someClass);

        // Discover test plugins
        for (TestPlugin testPlugin : ServiceLoader.load(TestPlugin.class,
                ClassLoaders.findMostCompleteClassLoader(JUnit4Runner.class))) {
            if (testPlugin.enabled(testContext)) {
                plugins.add(testPlugin);
            }
        }
        sortByPriority(plugins, PriorityUtils::priorityOfClassOf);

        // Discover decorators
        plugins.forEach(plugin -> decorators.addAll(plugin.decorators()));
        sortByPriority(decorators, PriorityUtils::priorityOf);

        // Determine launch mode
        launchMode = gatherLaunchMode();
    }

    @Override
    public void run(RunNotifier notifier) {
        try {
            if (launchMode == LaunchMode.PER_TEST_CLASS) {
                doStart();
            }

            super.run(notifier);

            if (launchMode == LaunchMode.PER_TEST_CLASS) {
                doStop();
            }
        } catch (StoppedByUserException e) {
            throw e;
        } catch (Throwable t) {
            notifyFailure(t, notifier);
        }
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        testContext.setTestMethod(method.getMethod());
        List<? extends TestDecorator> decoratorInstances = decorators.stream()
                .map(this::instantiate)
                .collect(Collectors.toList());
        try {
            if (launchMode == LaunchMode.PER_TEST) {
                doStart();
            }

            try {
                // Invoke decorator beforeTest()
                decoratorInstances.forEach(decorator -> decorator.beforeTest(testContext));
                super.runChild(method, notifier);
            } finally {
                // Invoke decorator afterTest()
                decoratorInstances.forEach(decorator -> decorator.afterTest(testContext));
            }
        } catch (Throwable t) {
            notifyFailure(t, notifier);
        } finally {
            if (launchMode == LaunchMode.PER_TEST) {
                doStop();
            }
            testContext.setTestMethod(null);
        }
    }

    @Override
    protected Object createTest() {
        return instantiate(testContext.testClass());
    }

    private LaunchMode gatherLaunchMode() {
        LaunchMode launchMode = LaunchMode.ANY;

        for (TestPlugin plugin : plugins) {
            LaunchMode requestedLaunchMode = plugin.launchMode(testContext);
            switch (requestedLaunchMode) {
                case NONE:
                    if (launchMode == LaunchMode.PER_TEST_CLASS || launchMode == LaunchMode.PER_TEST) {
                        throw SeedException.createNew(JUnit4ErrorCode.CONFLICTING_LAUNCH_MODES)
                                .put("requestedLaunchMode", LaunchMode.NONE)
                                .put("existingLaunchMode", String.valueOf(launchMode))
                                .put("test", testContext.testName());
                    }
                    launchMode = LaunchMode.NONE;
                    break;
                case PER_TEST_CLASS:
                    if (launchMode == LaunchMode.NONE || launchMode == LaunchMode.PER_TEST) {
                        throw SeedException.createNew(JUnit4ErrorCode.CONFLICTING_LAUNCH_MODES)
                                .put("requestedLaunchMode", LaunchMode.PER_TEST_CLASS)
                                .put("existingLaunchMode", String.valueOf(launchMode))
                                .put("test", testContext.testName());
                    }
                    launchMode = LaunchMode.PER_TEST_CLASS;
                    break;
                case PER_TEST:
                    if (launchMode == LaunchMode.NONE || launchMode == LaunchMode.PER_TEST_CLASS) {
                        throw SeedException.createNew(JUnit4ErrorCode.CONFLICTING_LAUNCH_MODES)
                                .put("requestedLaunchMode", LaunchMode.PER_TEST)
                                .put("existingLaunchMode", String.valueOf(launchMode))
                                .put("test", testContext.testName());
                    }
                    launchMode = LaunchMode.PER_TEST;
                    break;
                default:
                    break;
            }
        }

        // Still undetermined after plugins -> PER_TEST_CLASS by default
        if (launchMode == LaunchMode.ANY) {
            launchMode = LaunchMode.PER_TEST_CLASS;
        }

        return launchMode;
    }

    private SeedLauncher gatherLauncher() {
        SeedLauncher launcher = null;

        for (TestPlugin plugin : plugins) {
            Optional<? extends SeedLauncher> requestedLauncher = plugin.launcher(testContext);
            if (requestedLauncher.isPresent()) {
                if (launcher == null) {
                    launcher = requestedLauncher.get();
                } else {
                    throw SeedException.createNew(JUnit4ErrorCode.CONFLICTING_LAUNCHERS)
                            .put("requestedLauncher", requestedLauncher.get().getClass())
                            .put("existingLauncherClass", launcher.getClass())
                            .put("test", testContext.testName());
                }
            }
        }

        if (launcher == null) {
            if (DEFAULT_IT_LAUNCHER_CLASS == null) {
                throw SeedException.createNew(JUnit4ErrorCode.MISSING_LAUNCHER_FOR_TEST)
                        .put("test", testContext.testName());
            } else {
                return Classes.instantiateDefault(DEFAULT_IT_LAUNCHER_CLASS);
            }
        } else {
            return launcher;
        }
    }

    private <T> T instantiate(Class<T> someClass) {
        return Optional.ofNullable(seedLauncher)
                .flatMap(SeedLauncher::getKernel)
                .filter(Kernel::isStarted)
                .map(kernel -> kernel.objectGraph().as(Injector.class).getInstance(someClass))
                .orElseGet((Throwing.Supplier<T, Exception>) () -> {
                    LOGGER.warn(
                            "No kernel available to create the test instance: injection and interception " +
                                    "unavailable.");
                    return Classes.instantiateDefault(someClass);
                });
    }

    private void notifyFailure(Throwable t, RunNotifier notifier) {
        notifier.fireTestFailure(new Failure(getDescription(), t));
    }

    private void doStart() {
        try {
            // Invoke plugin beforeLaunch()
            plugins.forEach(plugin -> plugin.beforeLaunch(testContext));

            Exception occurredException = null;
            try {
                seedLauncher = gatherLauncher();
                seedLauncher.launch(gatherCliArguments(), gatherKernelParameters());
            } catch (Exception e) {
                occurredException = e;
            }

            Class<? extends Exception> expectedException = gatherExpectedException();
            if (expectedException == null) {
                if (occurredException != null) {
                    throw SeedException.wrap(occurredException, JUnit4ErrorCode.FAILED_TO_LAUNCH_APPLICATION)
                            .put("test", testContext.testName());
                }
            } else {
                if (occurredException == null) {
                    throw SeedException.createNew(JUnit4ErrorCode.EXPECTED_EXCEPTION_DID_NOT_OCCURRED)
                            .put("expectedClass", expectedException)
                            .put("test", testContext.testName());
                } else if (!expectedException.isAssignableFrom(occurredException.getClass())) {
                    throw SeedException.wrap(occurredException,
                            JUnit4ErrorCode.ANOTHER_EXCEPTION_THAN_EXPECTED_OCCURRED)
                            .put("expectedClass", expectedException)
                            .put("test", testContext.testName());
                }
            }

            if (seedLauncher != null) {
                seedLauncher.getKernel().ifPresent(testContext::setKernel);
            }
        } catch (Exception e1) {
            // Attempt to stop anything that could have already started
            try {
                doStop();
            } catch (Exception e2) {
                // do nothing
            }
            throw e1;
        }
    }

    private String[] gatherCliArguments() {
        String[] args = null;
        for (TestPlugin plugin : plugins) {
            String[] pluginArgs = plugin.arguments(testContext);
            if (pluginArgs != null && pluginArgs.length > 0) {
                if (args == null) {
                    args = pluginArgs.clone();
                } else {
                    throw SeedException.createNew(JUnit4ErrorCode.CONFLICTING_ARGUMENTS)
                            .put("requestedArguments", Arrays.toString(pluginArgs))
                            .put("existingArguments", Arrays.toString(args))
                            .put("test", testContext.testName());
                }
            }
        }
        return args == null ? new String[0] : args;
    }

    private Map<String, String> gatherKernelParameters() {
        Map<String, String> kernelParameters = new HashMap<>();
        for (TestPlugin plugin : plugins) {
            Map<String, String> pluginKernelParameters = plugin.kernelParameters(testContext);
            if (pluginKernelParameters != null && !pluginKernelParameters.isEmpty()) {
                kernelParameters.putAll(pluginKernelParameters);
            }

            Map<String, String> pluginConfigurationProperties = plugin.configurationProperties(testContext);
            if (pluginConfigurationProperties != null) {
                for (Map.Entry<String, String> configProperty : pluginConfigurationProperties.entrySet()) {
                    kernelParameters.put(CONFIG_PREFIX + configProperty.getKey(), configProperty.getValue());
                }
            }
        }
        return kernelParameters;
    }

    private Class<? extends Exception> gatherExpectedException() {
        Class<? extends Exception> expectedException = null;
        for (TestPlugin plugin : plugins) {
            Optional<Class<? extends Exception>> pluginExpectedException = plugin.expectedException(testContext);
            if (pluginExpectedException.isPresent()) {
                if (expectedException == null) {
                    expectedException = pluginExpectedException.get();
                } else {
                    throw SeedException.createNew(JUnit4ErrorCode.CONFLICTING_EXPECTED_EXCEPTIONS)
                            .put("requestedException", pluginExpectedException)
                            .put("existingException", expectedException)
                            .put("test", testContext.testName());
                }
            }
        }
        return expectedException;
    }

    private void doStop() {
        try {
            if (seedLauncher != null) {
                seedLauncher.shutdown();
            }
        } catch (Exception e) {
            throw SeedException.wrap(e, JUnit4ErrorCode.FAILED_TO_SHUTDOWN_APPLICATION)
                    .put("test", testContext.testName());
        } finally {
            testContext.setKernel(null);
            seedLauncher = null;

            // Invoke plugin afterShutdown()
            plugins.forEach(plugin -> plugin.afterShutdown(testContext));
        }
    }
}
