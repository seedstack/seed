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
