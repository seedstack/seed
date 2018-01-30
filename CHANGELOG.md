# Version 3.5.0 (2018-02-28)

* [new] Java 9 compatibility.
* [new] JAX-RS 2.1 support.
* [new] Integration testing now uses SeedStack launchers to execute the tested application.
* [new] Ability to choose the tested application launcher with `@LaunchWith`
* [new] Ability to define/override system properties for integration testing with `@SystemProperty`.
* [new] Ability to define/override kernel parameters for integration testing with `@KernelParameter`.
* [new] Ability to define/override configuration for integration testing with `@ConfigurationProperty`.
* [new] Ability to select configuration profiles for integration testing with `@ConfigurationProfiles`.
* [new] Ability to specify launch arguments for integration testing with `@Arguments`.
* [new] Run Web integration tests with undertow by combining `@RunWith(SeedITRunner.class)` and `@LaunchWithUndertow`.
* [brk] The integration testing API has been refactored to support other testing frameworks. 
* [fix] Defer JNDI lookup through `@Resource` annotation until the instance containing the injection is created.
* [fix] Fix lack of injection in custom ConstraintValidators.

# Version 3.4.2 (2018-01-12)

* [new] Add configuration watching for local files and automatic refresh after change (enable by setting config property `config.watch` to true).
* [fix] Fix `config` tool NullPointerException when dumping a config tree with generics and no null value.
* [fix] Fix exception when a `@CliCommand`-annotated class inherits from a base class. 

# Version 3.4.1 (2017-11-29)

* [fix] Undertow-based applications would not refresh after a startup failure.

# Version 3.4.0 (2017-11-28)

* [new] Validation exceptions on REST resources are automatically mapped to a detailed response.
* [new] A `@RequiresCrudPermissions` annotation allows to add permission checks based on the detected CRUD action of the called method.
* [new] SPI `CrudActionResolver` has been added to security to allow for resolving the CRUD action of a particular method.
* [new] Provides the ability to configure some Shiro implementation classes: `SubjectDAO`, `SubjectFactory`, `SubjectContext`, `Authenticator`, `AuthenticationStrategy`, `CacheManager` and `SessionStorageEvaluator`.  
* [new] A JAX-RS implementation of `CrudActionResolver` detects the CRUD action based upon the JAX-RS annotations.
* [new] Basic support for refreshing Web applications served with Undertow.
* [fix] Prevent session fixation issue by regenerating the session (if any) upon successful login.
* [fix] Make `SimplePrincipalProvider` serializable. 
* [chg] Security sessions are now enabled by default.
* [brk] Data import/export API has been removed and replaced by a newer API into business framework.

# Version 3.3.1 (2017-09-06)

* [new] Configuration dump (`config` tool) now dumps inner properties for maps, collections, arrays and complex objects.
* [new] Add `beforeInitialization()` and `afterInitialization()` methods on `SeedInitializer` interface.
* [new] Add `isRemembered()` on `SecuritySupport` interface.
 
# Version 3.3.0 (2017-07-31)

* [new] Print a default banner at startup in case of missing custom `banner.txt`.
* [new] Add `application.colorOuput` configuration property to force the color output mode (AUTODETECT, PASSTHROUGH, ENABLE, DISABLE).
* [new] Any singleton implementing `AutoCloseable` will have its `close()` method invoked at application shutdown (can be ignored with @Ignore).
* [new] Overriding Guice modules can be installed by using `@Install(override = true)`.
* [new] The `@Bind` annotation allows to easily define arbitrary bindings by annotating implementations.  
* [new] The `@ITBind` and `@ITInstall` annotations have been updated to allow the same options as `@Bind` and `@Install`.  
* [new] Can now read transaction metadata from JTA 1.2 `@Transactional` annotation.  
* [new] Web session tracking mode is now set to COOKIE by default (a `web.sessionTrackingMode` configuration option allows to change it).
* [new] Add `security.web.successUrl` and `security.web.logoutUrl` configuration options to configure redirection after, respectively, successful login and logout.
* [new] Add `security.web.form` configuration object to configure form-based authentication.
* [brk] Deprecated `expand()` method of `org.seedstack.seed.rest.hal.Link` has been removed (use getHref() instead).
* [fix] Avoid NullPointerException when an exception occurs in a `NOT_SUPPORTED` local transaction.
* [chg] Overriding an more general class configuration attribute with a more specific null-valued one, completely removes the attribute.
* [chg] HTTP/2 is enabled by default with Undertow.
* [chg] Update Guice to [4.1.0](https://github.com/google/guice/wiki/Guice41).
* [chg] Update Shiro to [1.4.0](https://issues.apache.org/jira/secure/ReleaseNote.jspa?projectId=12310950&version=12338814).
* [chg] Update Jersey to [2.25.1](https://jersey.java.net/release-notes/2.25.1.html).
* [chg] Update Hibernate validator to [5.4.1.Final](https://github.com/hibernate/hibernate-validator/blob/5.4.1.Final/changelog.txt).
* [chg] Update Commons CLI to [1.4](https://commons.apache.org/proper/commons-cli/changes-report.html#a1.4).
* [chg] Update Undertow to 1.4.14.

# Version 3.2.0 (2017-04-28)

* [new] Add `SeedInitializer` interface that is called at Seed JVM-wide initialization and close (can be used to do early initialization). 
* [new] Tools can now be run in two modes: with the minimal set of plugins (minimal) or with all plugins loaded (full). 
* [fix] Fix StackOverflowError under Tomcat in Eclipse WTP when auto-configuring Logback.
* [fix] Fix resolution of `WEB-INF/classes` under Tomcat 8 when using resource overlay (PreResources, PostResources).
* [fix] Properly logout subject when testing with `@WithUser`.
* [fix] Prevent WebResourceFilter from serving files under `/WEB-INF`, allowing JSP to be served correctly.
* [fix] Fix NPE when a filter or a servlet wa*s already registered by the container.
* [chg] Default diagnostic dump changed from JSON to YAML.
* [chg] Using the application classloader to load properties for additional JNDI contexts (paths should not start with `/` anymore).
* [chg] Configuration properties files are now mapped in depth (the dot-notation in the property key is used to create intermediary tree nodes).

# Version 3.1.0 (2017-02-16)

* [new] Configuration can be sourced from properties files (`application.properties` and `META-INF/configuration/*.properties`).
* [new] The `effective-config` tool dumps the aggregated global configuration tree of the application.
* [new] The `crypt` tool crypts values using the master key store.
* [new] Support for configuration profiles (specify profiles with system property `seedstack.profiles`).
* [new] Sensitive information is hidden in configuration dumps (properties named `passwd`, `password`, `pwd`, system properties and environment variables)
* [new] Ported security cache from version 2.3.4.
* [new] Ported Jersey2 cache control from version 2.3.3.
* [fix] Fix mishandling of override scanned configuration files.
* [fix] Configuration was incorrectly refreshed at every access. 
* [chg] coffig: **[2.0.0](https://github.com/seedstack/shed/releases/tag/v2.0.0)**

# Version 3.0.3 (2017-01-16)

* [chg] shed: **[1.0.1](https://github.com/seedstack/shed/releases/tag/v1.0.1)**

# Version 3.0.2 (2017-01-13)

* [fix] Fix `ClassNotFoundException` in `CliConfig` due to wrong import. 

# Version 3.0.1 (2017-01-12)

* [fix] Application name was not reflecting application id change when no custom name specified.
* [fix] Remove irrelevant log info about the disabled data security when no EL present.
* [fix] Properly notify test initialization failures to JUnit.
* [chg] Inject a default value in fields annotated with `@Configuration` if they are null in the first place.
* [chg] Update coffig to [1.1.0](https://github.com/seedstack/coffig/releases/tag/v1.1.0).

# Version 3.0.0 (2016-12-12)

* [new] New configuration system based on [Coffig library](https://github.com/seedstack/coffig).
* [new] Tooling support allows to launch tools from the command-line (-Dseedstack.tool=<toolName>) or from the SeedStack maven plugin.
* [new] The `config` tool dumps the current configuration options for the application.
* [new] The `errors` tool dumps all current error messages available in the application.
* [new] Kernel parameter `seedstack.autodetectModules` allows to control if module detection should be done (true/false).
* [new] HTTP(S) proxy automatic detection or explicit configuration support. 
* [brk] Java 8 is now required.
* [brk] Classic configuration system based on props has been removed in favor of a YAML configuration (amongst others).
* [brk] Configuration keys have been shortened and renamed.
* [brk] The `password` configuration lookup has been replaced by the `$decrypt` configuration function.
* [brk] Jersey 1 implementation of JAX-RS has been removed.
* [brk] Class `org.seedstack.seed.cli.SeedRunner` was renamed to `org.seedstack.seed.cli.CliLauncher` and its main method has been removed (use `org.seedstack.seed.core.SeedMain` instead).
* [brk] The `transaction`, `el` and `crypto` modules are merged into `core` module.
* [brk] The `shell` module is now an add-on.
* [brk] Even when only one transaction handler is present, it is no longer automatically used (an explicit resource must always be specified for the transaction).
* [brk] The default value of the `@Configuration` annotation `mandatory` argument has been changed to `false`.
* [brk] The `defaultValue` attribute of the `@Configuration` annotation has been removed. Pre-initialize fields if a default value is needed.
* [brk] The package `org.seedstack.seed.core.utils` has been removed in favor of the 'shed' utility library.
* [brk] Annotation resolution strategy has been unified for all SeedStack annotations and can exhibit minor differences with prior strategies.

# Version 2.3.4 (2017-02-15)

* [new] Add security cache for authentication (enabled by default, can be disabled with `org.seedstack.seed.security.cache.authentication.enabled = false`)
* [new] Add security cache for authorization (enabled by default, can be disabled with `org.seedstack.seed.security.cache.authorization.enabled = false`)

# Version 2.3.3 (2017-01-24)

* [fix] JAX-RS resources were not decorated with cache busting headers when using Jersey2 implementation.
* [fix] Jersey2 module did not honor the `@CacheControl` annotation.
* [brk] `@ResourceFiltering` annotation is specific to Jersey1 and has been moved to package `org.seedstack.seed.rest.jersey1` accordingly.

# Version 2.3.2 (2016-11-09)

* [fix] Fix colors under Windows command-line
* [fix] Fix wrong priority of CORS filter which was below security filter.
* [fix] Fix binding of `X509CertificateFilter` which was bound multiple times.

# Version 2.3.1 (2016-09-07)

* [chg] Annotated WebSockets are now registered programatically as it allows injection of the configurator class.
* [fix] Catch exception thrown by the JAnsi library when used on an unsupported OS (i.e. not linux/windows/osx).  

# Version 2.3.0 (2016-04-25)

* [new] Full compatibility with Java 8.
* [new] Interface `LifecycleListener` provides the ability to execute code upon application startup and shutdown.
* [new] Global facade (class `Seed`) for kernel creation and disposal.
* [new] Auto-configuration of Logback when it is in use and no `logback.xml` file exists.
* [new] Best-effort to detect console color output in various runtime environments.
* [new] Ability to print a custom banner upon startup by providing a `banner.txt` file in the default package.
* [chg] Update to official Guice 4.0 (not using Sisu-Guice anymore).
* [chg] Improve log traces on startup errors.
* [chg] Better default log format.
* [chg] Update parent pom to [2.4.0](https://github.com/seedstack/poms/releases/tag/v2.4.0)
* [chg] Update `commons-configuration` to 1.10.
* [chg] Update `commons-cli` to 1.3.1.
* [chg] Update `shiro` to 1.2.4.
* [chg] Update `undertow` to 1.3.19.Final.
* [chg] Update `jodd` to 3.6.6.
* [chg] Update `metrics` to 3.1.2.
* [chg] Update `jersey1` to 1.19.1.
* [chg] Update `jersey2` to 2.22.2.
* [fix] Correctly injects `@Logging`-annotated inherited loggers.

## Web

* [new] Fully-injectable and interceptable servlets, filters and listeners.
* [new] Full compatibility with asynchronous servlets and filters.
* [new] Ability to programatically register servlets, filters and listeners.
* [chg] WebSocket support, previously in `seed-web-websocket` module is merged into `seed-web-core` module.
* [brk] Compatibility with Servlet 2.5 is dropped.
* [brk] Custom Servlet annotations (`@WebServlet`, `@WebFilter` and `@WebListener`) are dropped in favor or standard ones.

## Rest

* [new] Full support for JAX-RS 2 asynchronous resources.
* [new] Detection of BeanParam classes in HAL scanner.
* [chg] Automatically prepends the servlet context path to generated HAL links.

## EL

* [new] Add support for Expression Language 3

## Testing

* [fix] Correctly take inheritance into account in expected IT exceptions
* [chg] Update Tomcat version to 8.0.32 for Arquillian tests.

# Version 2.2.1 (2016-03-22)

## Rest

* [new] Support configuration of Jersey 2 features. Automatically enable multipart and JSP features if detected on the classpath.
* [new] Add multipart feature as a dependency of Jersey 2 module, enabling it by default.

# Version 2.2.0 (2016-01-28)

* [fix] Fix the `@Ignore` annotation which was not working anymore in version 2.1.0.

## Rest

* [chg] `RelRegistry` automatically prepends the servlet context path to generated HAL links.

## Web

* [new] JAX-RS 2 support through Jersey 2.
* [new] Applications can launch in a Servlet 3+ environment without web.xml file.
* [brk] Remove the `org.seedstack.seed.web.DelegateServletContextListener` interface which can be replaced by native servlet listeners.

## Security

* [chg] Disable storage of security sessions by default (can be re-enabled by setting `org.seedstack.seed.security.sessions.enabled` to true)
* [chg] Sets the default security session timeout to 15 minutes (instead of 30 minutes before) when sessions are enabled
* [new] Security session timeout can be changed with the `org.seedstack.seed.security.sessions.timeout` property (in seconds).
* [new] Add an anti-XSRF security filter (named `xsrf`) which can be used in Web security filter chains to prevent XSRF attacks.

# Version 2.1.0 (2015-11-26)

* [brk] Merged dedicated test modules into with their core implementation.
* [brk] Merged multiple testing modules into a unique one named `seed-testing`.
* [brk] Simplified the naming convention of all modules by getting rid of the `support` word.
* [brk] Simplified the framework by factoring-out numerous modules as SeedStack add-ons (http://seedstack.org/addons/).
* [new] Final version of cryptography support in the `seed-crypto` module.

## Testing

* [brk] Changed the SPI of integration testing plugins.

## Web

* [new] Added Undertow embedded-server support in `seed-web-undertow` module. 
* [brk] Moved Jersey 1 implementation in its own module `seed-rest-jersey1`.

# Version 2.0.0 (2015-07-30)

* [new] Initial Open-Source release.
