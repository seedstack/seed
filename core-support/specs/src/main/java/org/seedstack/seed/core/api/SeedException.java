/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.api;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.seedstack.seed.core.utils.SeedStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This is the base class for all technical SEED exceptions.
 *
 * @author epo.jemba@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 */
public class SeedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SeedException.class);
    private static final String INITIAL_LOCALE_CODE = Locale.getDefault().getLanguage();
    private static final ConcurrentMap<String, Map<String, String>> ERROR_TEMPLATES = new ConcurrentHashMap<String, Map<String, String>>();
    private static final int WRAP_LENGTH = 120;
    private static final String CAUSE_PATTERN = "%d. %s";
    private static final String CODE_PATTERN = "(%s) %s";
    private static final String ERROR_TEMPLATE_PATH = "META-INF/errors/";
    private static final String ERROR_TEMPLATE_EXTENSION = ".properties";

    private final ErrorCode errorCode;
    private final Map<String, Object> properties = new HashMap<String, Object>();

    private boolean alreadyComputed = false;
    private List<String> causes;
    private String fix;
    private String url;

    protected SeedException(ErrorCode errorCode) {
        super(formatErrorCode(errorCode));
        this.errorCode = errorCode;
    }

    protected SeedException(ErrorCode errorCode, Throwable cause) {
        super(formatErrorCode(errorCode), cause);
        this.errorCode = errorCode;
    }

    /**
     * Retrieve the {@link ErrorCode} of this exception.
     *
     * @return the error code instance.
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Return the properties of this exception.
     *
     * @return the map of the properties.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Return a property of this exception.
     *
     * @param name the name of the property.
     * @param <T>  the type of the property.
     * @return the value of the property.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T) properties.get(name);
    }

    /**
     * Put a property in this exception.
     *
     * @param name  the name of the property.
     * @param value the value of the property.
     * @param <E>   the type fo the property.
     * @return this exception (to chain calls).
     */
    @SuppressWarnings("unchecked")
    public <E extends SeedException> E put(String name, Object value) {
        properties.put(name, value);
        return (E) this;
    }

    /**
     * Retrieve the SEED message associated with this exception.
     *
     * @return the SEED message if any, null otherwise.
     */
    public String getSeedMessage() {
        String template = getErrorTemplate("message");
        if (template != null) {
            return SeedStringUtils.replaceTokens(template, properties);
        }

        return null;
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        synchronized (s) {
            s.println(this);
            s.println(buildStackTrace());
        }
    }

    @Override
    public void printStackTrace(PrintStream s) {
        synchronized (s) {
            s.println(this);
            s.println(buildStackTrace());
        }
    }

    public List<String> getCauses() {
        compute();
        return this.causes;
    }

    public String getFix() {
        compute();
        return this.fix;
    }

    public String getUrl() {
        compute();
        return this.url;
    }

    private String buildStackTrace() {
        StringBuilder s = new StringBuilder();

        s.append(StringUtils.leftPad("", SeedException.class.getCanonicalName().length() + getMessage().length() + 2, "="));
        s.append("\n");

        String seedMessage = getSeedMessage();
        if (seedMessage != null) {
            s.append(wrapLine(seedMessage));
            s.append("\n\n");
        } else {
            s.append("\n");
        }

        compute();
        int i = causes.size();
        if (i == 0) {
            s.append("No cause found\n--------------\n\n");
        } else if (i == 1) {
            s.append("Cause\n-----\n");
            s.append(wrapLine(causes.get(0)));
            s.append("\n\n");
        } else {
            s.append("Causes\n------\n");

            int count = 1;
            for (String seedCause : causes) {
                s.append(wrapLine(String.format(CAUSE_PATTERN, count, seedCause)));
                count++;
            }

            s.append("\n");
        }

        if (fix != null) {
            s.append("Fix\n---\n");
            s.append(wrapLine(fix));
            s.append("\n");
        }

        if (url != null) {
            s.append("Online information\n------------------\n");
            s.append(url).append("\n");
            s.append("\n");
        }

        s.append("Detailed trace\n--------------\n");
        StringWriter stringWriter = new StringWriter();
        super.printStackTrace(new PrintWriter(stringWriter));
        s.append(stringWriter.toString());
        s.append("\n");

        return s.toString();
    }

    private StringBuffer wrapLine(String seedMessage) {
        StringBuffer sb = new StringBuffer();
        if (seedMessage != null && !"".equals(seedMessage)) {
            String[] split = seedMessage.split("\n");
            for (String s1 : split) {
                sb.append(WordUtils.wrap(s1, WRAP_LENGTH)).append('\n');
            }
        }
        return sb;
    }

    private void compute() {
        if (alreadyComputed) {
            return;
        }

        List<String> causes = new ArrayList<String>();
        String fix = null;
        String url = null;

        Throwable theCause = getCause();
        while (theCause != null) {
            if (theCause instanceof SeedException) {
                SeedException seedCause = (SeedException) theCause;

                // Find the fix at lowest depth
                fix = seedCause.getErrorTemplate("fix");
                if (fix != null) {
                    fix = SeedStringUtils.replaceTokens(fix, seedCause.getProperties());
                }

                // Also get the url
                url = seedCause.getErrorTemplate("url");
                if (url != null) {
                    url = SeedStringUtils.replaceTokens(url, seedCause.getProperties());
                }

                // Collects all cause messages from highest to lowest level
                String seedCauseErrorTemplate = seedCause.getErrorTemplate("message");
                if (seedCauseErrorTemplate != null) {
                    causes.add(String.format(CODE_PATTERN, formatErrorClass(seedCause.getErrorCode()), SeedStringUtils.replaceTokens(seedCauseErrorTemplate, seedCause.getProperties())));
                } else {
                    causes.add(theCause.getMessage());
                }
            } else {
                causes.add(theCause.toString());
            }

            theCause = theCause.getCause();
        }

        if (fix == null) {
            fix = getErrorTemplate("fix");
            if (fix != null) {
                fix = SeedStringUtils.replaceTokens(fix, getProperties());
            }
        }

        if (url == null) {
            url = getErrorTemplate("url");
            if (url != null) {
                url = SeedStringUtils.replaceTokens(url, getProperties());
            }
        }

        this.causes = causes;
        this.fix = fix;
        this.url = url;
        this.alreadyComputed = true;
    }

    /**
     * Throws this exception.
     */
    public void thenThrows() {
        throw this;
    }

    /**
     * Throws this exception if a condition is met.
     *
     * @param conditionToThrow the condition to assess.
     */
    public void throwsIf(boolean conditionToThrow) {
        if (conditionToThrow) {
            throw this;
        }
    }

    /**
     * Throws this exception if parameter is NOT null.
     *
     * @param conditionToThrow the parameter to check.
     */
    public void throwsIfNotNull(Object conditionToThrow) {
        if (conditionToThrow != null) {
            throw this;
        }
    }

    /**
     * Throws this exception if parameter is null.
     *
     * @param conditionToThrow the parameter to check.
     */
    public void throwsIfNull(Object conditionToThrow) {
        if (conditionToThrow == null) {
            throw this;
        }
    }

    private String getErrorTemplate(String templateType) {
        Map<String, String> templates = ERROR_TEMPLATES.get(errorCode.getClass().getCanonicalName());

        if (templates == null) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Properties loadedProperties = new Properties();

            if (classLoader == null) {
                classLoader = errorCode.getClass().getClassLoader();
            }

            if (classLoader != null) {
                String catalogPath = ERROR_TEMPLATE_PATH + errorCode.getClass().getCanonicalName() + "_" + INITIAL_LOCALE_CODE + ERROR_TEMPLATE_EXTENSION;
                InputStream errorTemplatesStream = classLoader.getResourceAsStream(catalogPath);

                if (errorTemplatesStream == null) {
                    catalogPath = ERROR_TEMPLATE_PATH + errorCode.getClass().getCanonicalName() + ERROR_TEMPLATE_EXTENSION;
                    errorTemplatesStream = classLoader.getResourceAsStream(catalogPath);
                }

                if (errorTemplatesStream != null) {
                    try {
                        loadedProperties.load(errorTemplatesStream);
                    } catch (IOException e) {
                        LOGGER.warn("Error reading error catalog for " + errorCode.getClass().getCanonicalName(), e);
                    }

                    try {
                        errorTemplatesStream.close();
                    } catch (IOException e) {
                        LOGGER.warn("Unable to close error catalog " + catalogPath, e);
                    }
                }
            }

            templates = new HashMap<String, String>();
            for (String name : loadedProperties.stringPropertyNames()) {
                templates.put(name, loadedProperties.getProperty(name));
            }

            ERROR_TEMPLATES.putIfAbsent(errorCode.getClass().getCanonicalName(), templates);
        }

        return templates.get(errorCode + "." + templateType);
    }

    private static String formatErrorCode(ErrorCode errorCode) {
        String name = errorCode.toString().toLowerCase().replace("_", " ");

        return String.format(
                CODE_PATTERN,
                formatErrorClass(errorCode),
                name.substring(0, 1).toUpperCase() + name.substring(1)
        );
    }

    private static String formatErrorClass(ErrorCode errorCode) {
        return errorCode.getClass().getSimpleName().replace("ErrorCodes", "").replace("ErrorCode", "").toUpperCase();
    }

    /**
     * Create a new SeedException from an {@link ErrorCode}.
     *
     * @param errorCode the error code to set.
     * @return the created SeedException.
     */
    public static SeedException createNew(ErrorCode errorCode) {
        return SeedException.createNew(SeedException.class, errorCode);
    }

    /**
     * Create a new subclass of SeedException from an {@link ErrorCode}.
     *
     * @param exceptionType the subclass of SeedException to create.
     * @param errorCode     the error code to set.
     * @param <E>           the subtype.
     * @return the created SeedException.
     */
    public static <E extends SeedException> E createNew(Class<E> exceptionType, ErrorCode errorCode) {
        try {
            Constructor<E> constructor = exceptionType.getDeclaredConstructor(ErrorCode.class);
            constructor.setAccessible(true);
            return constructor.newInstance(errorCode);
        } catch (Exception e) {
            throw new IllegalArgumentException(exceptionType.getCanonicalName() + " must implement a constructor with ErrorCode as parameter", e);
        }
    }

    /**
     * Wrap a SeedException with an {@link ErrorCode} around an existing {@link Throwable}.
     *
     * @param throwable the existing throwable to wrap.
     * @param errorCode the error code to set.
     * @return the created SeedException.
     */
    public static SeedException wrap(Throwable throwable, ErrorCode errorCode) {
        return SeedException.wrap(SeedException.class, throwable, errorCode);
    }

    /**
     * Wrap a subclass of SeedException with an {@link ErrorCode} around an existing {@link Throwable}.
     *
     * @param exceptionType the subclass of SeedException to create.
     * @param throwable     the existing throwable to wrap.
     * @param errorCode     the error code to set.
     * @param <E>           the subtype.
     * @return the created SeedException.
     */
    public static <E extends SeedException> E wrap(Class<E> exceptionType, Throwable throwable, ErrorCode errorCode) {
        try {
            Constructor<E> constructor = exceptionType.getDeclaredConstructor(ErrorCode.class, Throwable.class);
            constructor.setAccessible(true);
            return constructor.newInstance(errorCode, throwable);
        } catch (Exception e) {
            throw new IllegalArgumentException(exceptionType.getCanonicalName() + " must implement a constructor with an ErrorCode and a Throwable as parameters", e);
        }
    }
}
