/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

import org.seedstack.shed.text.TextUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.seedstack.shed.text.TextUtils.replaceTokens;

/**
 * This is the base class for all technical SeedStack exceptions. It provides additional information over traditional
 * exception: detailed message, fix advice and online URL. Extra attributes can be added to the exception and used in
 * message templates.
 */
public class SeedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private static final int WRAP_LENGTH = 120;
    private static final String CAUSE_PATTERN = "%d. %s";
    private static final String CODE_PATTERN = "(%s) %s";
    private static final String JAVA_LANG_THROWABLE = "java.lang.Throwable";
    private static final String PRINT_STACK_TRACE = "printStackTrace";
    private static final String CONSTRUCTOR = "<init>";
    private static final String COM_GOOGLE_INJECT_INTERNAL_ERRORS = "com.google.inject.internal.Errors";

    private final ErrorCode errorCode;
    private final Map<String, Object> properties = new HashMap<>();
    private final AtomicBoolean alreadyComputed = new AtomicBoolean(false);
    private final ThreadLocal<Boolean> alreadyVisited = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    private List<String> causes;
    private String message;
    private String fix;
    private String url;

    /**
     * Create a SeedException from an {@link ErrorCode}.
     *
     * @param errorCode the error code.
     */
    protected SeedException(ErrorCode errorCode) {
        super(formatErrorCode(errorCode));
        this.errorCode = errorCode;
    }

    /**
     * Create a SeedException from an {@link ErrorCode} wrapping a {@link Throwable}.
     *
     * @param errorCode the error code.
     * @param cause     the cause of this exception if any.
     */
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
     * The toString() method is overloaded to provide additional exception details. When invoked directly it only returns
     * the details of this exception. When invoked from printStackTrace() it returns the details of this exception and
     * flags all causes of SeedException type to only display their short message when their toString() method will be
     * invoked by printStacktrace(). This uses a ThreadLocal implementation of the flag to stay thread-safe.
     *
     * @return a textual representation of the exception.
     */
    @Override
    public String toString() {
        int location = getLocation();

        if (location == 1 || location == 3) {
            // if called from throwable constructor or from a verbose Guice exception
            // we return the simple message to avoid messing stack trace
            return getMessage();
        }

        if (location == 2) {
            // if called from printStackTrace() we ensure that only the first SeedException is fully displayed
            try {
                if (alreadyVisited.get()) {
                    // Already displayed in the cause list of the first SeedException
                    return super.toString();
                } else {
                    // First SeedException to be displayed in a causal chain
                    Throwable theCause = getCause();
                    while (theCause != null) {
                        if (theCause instanceof SeedException) {
                            ((SeedException) theCause).alreadyVisited.set(true);
                        }
                        theCause = theCause.getCause();
                    }
                }
            } finally {
                alreadyVisited.remove();
            }
        }

        compute();

        StringBuilder s = new StringBuilder(16384);

        s.append(super.toString());
        s.append("\n");

        String seedMessage = getDescription();
        if (seedMessage != null) {
            s.append("\nDescription\n-----------\n");
            s.append(wrapLine(seedMessage));
        }

        int i = causes.size();
        if (i == 1) {
            s.append("\nCause\n-----\n");
            s.append(wrapLine(causes.get(0)));
        } else if (i > 1) {
            s.append("\nCauses\n------\n");

            int count = 1;
            for (String seedCause : causes) {
                s.append(wrapLine(String.format(CAUSE_PATTERN, count, seedCause)));
                count++;
            }
        }

        if (fix != null) {
            s.append("\nFix\n---\n");
            s.append(wrapLine(fix));
        }

        if (url != null) {
            s.append("\nOnline information\n------------------\n");
            s.append(url).append("\n");
        }

        if (location == 2) {
            // this header is displayed only if called from printStackTrace()
            s.append("\nStacktrace\n----------");
        }

        return s.toString();
    }

    /**
     * Provides additional information beyond the short message.
     *
     * @return the exception description or null if none exists.
     */
    public String getDescription() {
        compute();
        return this.message;
    }

    /**
     * Provides a list describing the causes of this exception. This list is built by iterating through this exception
     * causes and storing the description through {@link #getDescription()} if present or the message through {@link #getMessage()}
     * as a fallback.
     *
     * @return the list of causes, possibly empty.
     */
    public List<String> getCauses() {
        compute();
        return this.causes;
    }

    /**
     * Provides advice on how to fix the root cause of the exception. This fix is effectively extracted from the last
     * cause available.
     *
     * @return the fix of the root cause or null if none exists.
     */
    public String getFix() {
        compute();
        return this.fix;
    }

    /**
     * Provides an URL to online information about the root cause of the exception. This URL is effectively extracted from the
     * last cause available.
     *
     * @return the online information URL of the root cause or null if none exists.
     */
    public String getUrl() {
        compute();
        return this.url;
    }

    private void compute() {
        if (alreadyComputed.getAndSet(true)) {
            return;
        }

        causes = new ArrayList<>();

        Throwable theCause = getCause();
        while (theCause != null) {
            if (theCause instanceof SeedException) {
                SeedException seedCause = (SeedException) theCause;

                // Find the fix at lowest depth
                String fixTemplate = seedCause.getTemplate("fix");
                if (fixTemplate != null) {
                    fix = replaceTokens(fixTemplate, seedCause.getProperties());
                }

                // Also get the url
                String urlTemplate = seedCause.getTemplate("url");
                if (urlTemplate != null) {
                    url = replaceTokens(urlTemplate, seedCause.getProperties());
                }

                // Collects all cause messages from highest to lowest level
                String seedCauseErrorTemplate = seedCause.getTemplate(null);
                if (seedCauseErrorTemplate != null) {
                    causes.add(String.format(CODE_PATTERN, formatErrorClass(seedCause.getErrorCode()), replaceTokens(seedCauseErrorTemplate, seedCause.getProperties())));
                } else {
                    causes.add(theCause.getMessage());
                }
            } else {
                causes.add(theCause.toString());
            }

            theCause = theCause.getCause();
        }

        if (message == null) {
            String messageTemplate = getTemplate(null);
            if (messageTemplate != null) {
                message = replaceTokens(messageTemplate, getProperties());
            }
        }

        if (fix == null) {
            String fixTemplate = getTemplate("fix");
            if (fixTemplate != null) {
                fix = replaceTokens(fixTemplate, getProperties());
            }
        }

        if (url == null) {
            String urlTemplate = getTemplate("url");
            if (urlTemplate != null) {
                url = replaceTokens(urlTemplate, getProperties());
            }
        }
    }

    private String getTemplate(String key) {
        try {
            return ResourceBundle
                    .getBundle(errorCode.getClass().getName())
                    .getString(key == null ? errorCode.toString() : errorCode.toString() + "." + key);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    private StringBuffer wrapLine(String seedMessage) {
        StringBuffer sb = new StringBuffer();
        if (seedMessage != null && !"".equals(seedMessage)) {
            String[] split = seedMessage.split("\n");
            for (String s1 : split) {
                sb.append(TextUtils.wrap(s1, WRAP_LENGTH)).append('\n');
            }
        }
        return sb;
    }

    private int getLocation() {
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            // In Throwable constructor
            if (JAVA_LANG_THROWABLE.equals(stackTraceElement.getClassName()) && CONSTRUCTOR.equals(stackTraceElement.getMethodName())) {
                return 1;
            }
            // In Throwable printStackTrace
            if (JAVA_LANG_THROWABLE.equals(stackTraceElement.getClassName()) && PRINT_STACK_TRACE.equals(stackTraceElement.getMethodName())) {
                return 2;
            }
            // In a Guice verbose exception
            if (COM_GOOGLE_INJECT_INTERNAL_ERRORS.equals(stackTraceElement.getClassName())) {
                return 3;
            }
        }
        // Elsewhere
        return 0;
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
        return new SeedException(errorCode);
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
        return new SeedException(errorCode, throwable);
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
