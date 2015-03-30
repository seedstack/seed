/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.internal.jms;

import org.apache.commons.lang.StringUtils;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;

final class SoapJmsUri {
    public static final String JNDI_LOOKUP_VARIANT = "jndi";
    public static final String SEED_QUEUE_LOOKUP_VARIANT = "queue";
    public static final String SEED_TOPIC_LOOKUP_VARIANT = "topic";

    private final String lookupVariant;
    private final String destinationName;
    private final Map<String, String[]> params;
    private final String uri2string;

    private SoapJmsUri(String lookupVariant, String destinationName, Map<String, String[]> params, String uri2string) {
        this.lookupVariant = lookupVariant;
        this.destinationName = destinationName;
        this.params = params;
        this.uri2string = uri2string;
    }

    static Map<String, String[]> parseUrlQueryString(String s) {
        if (s == null) return new HashMap<String, String[]>(0);
        // In map1 we use strings and ArrayLists to collect the parameter values.
        HashMap<String, Object> map1 = new HashMap<String, Object>();
        int p = 0;
        while (p < s.length()) {
            int p0 = p;
            while (p < s.length() && s.charAt(p) != '=' && s.charAt(p) != '&') p++;
            String name = urlDecode(s.substring(p0, p));
            if (p < s.length() && s.charAt(p) == '=') p++;
            p0 = p;
            while (p < s.length() && s.charAt(p) != '&') p++;
            String value = urlDecode(s.substring(p0, p));
            if (p < s.length() && s.charAt(p) == '&') p++;
            Object x = map1.get(name);
            if (x == null) {
                // The first value of each name is added directly as a string to the map.
                map1.put(name, value);
            } else if (x instanceof String) {
                // For multiple values, we use an ArrayList.
                ArrayList<String> a = new ArrayList<String>();
                a.add((String) x);
                a.add(value);
                map1.put(name, a);
            } else {
                @SuppressWarnings("unchecked")
                ArrayList<String> a = (ArrayList<String>) x;
                a.add(value);
            }
        }
        // Copy map1 to map2. Map2 uses string arrays to store the parameter values.
        HashMap<String, String[]> map2 = new HashMap<String, String[]>(map1.size());
        for (Map.Entry<String, Object> e : map1.entrySet()) {
            String name = e.getKey();
            Object x = e.getValue();
            String[] v;
            if (x instanceof String) {
                v = new String[]{(String) x};
            } else {
                @SuppressWarnings("unchecked")
                ArrayList<String> a = (ArrayList<String>) x;
                v = new String[a.size()];
                v = a.toArray(v);
            }
            map2.put(name, v);
        }
        return map2;
    }

    static String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error in urlDecode.", e);
        }
    }

    static SoapJmsUri parse(URI uri) {
        if (!"jms".equals(uri.getScheme())) {
            throw new IllegalArgumentException("Not a valid SOAP JMS URI " + uri.toString());
        }

        String ssp = uri.getSchemeSpecificPart();
        String[] splitSsp = ssp.split(":", 2);

        if (splitSsp.length != 2) {
            throw new IllegalArgumentException("Invalid SOAP JMS URI " + splitSsp[0]);
        }

        String[] splitSecondPart = splitSsp[1].split("\\?", 2);

        if (splitSecondPart.length != 2) {
            throw new IllegalArgumentException("Invalid SOAP JMS URI " + splitSsp[1]);
        }

        return new SoapJmsUri(splitSsp[0], splitSecondPart[0], parseUrlQueryString(splitSecondPart[1]), uri.toASCIIString());
    }

    static Context getContext(SoapJmsUri soapJmsUri) throws NamingException {
        String jndiInitialContextFactory = soapJmsUri.getParameter("jndiInitialContextFactory");
        if (StringUtils.isEmpty(jndiInitialContextFactory)) {
            throw new IllegalArgumentException("Missing SOAP-JMS URI parameter jndiInitialContextFactory");
        }

        String jndiURL = soapJmsUri.getParameter("jndiURL");
        if (StringUtils.isEmpty(jndiURL)) {
            throw new IllegalArgumentException("Missing SOAP-JMS URI parameter jndiURL");
        }

        Properties properties = new Properties();
        properties.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, jndiInitialContextFactory);
        properties.setProperty(javax.naming.Context.PROVIDER_URL, jndiURL);

        return new InitialContext(properties);
    }

    static Destination getDestination(SoapJmsUri soapJmsUri, Session session) throws NamingException, JMSException {
        Destination destination;


        String lookupVariant = soapJmsUri.getLookupVariant();
        if (SoapJmsUri.JNDI_LOOKUP_VARIANT.equals(lookupVariant)) {
            destination = (Destination) getContext(soapJmsUri).lookup(soapJmsUri.getDestinationName());
        } else if (SoapJmsUri.SEED_QUEUE_LOOKUP_VARIANT.equals(lookupVariant)) {
            destination = session.createQueue(soapJmsUri.getDestinationName());
        } else if (SoapJmsUri.SEED_TOPIC_LOOKUP_VARIANT.equals(lookupVariant)) {
            destination = session.createTopic(soapJmsUri.getDestinationName());
        } else {
            throw new IllegalArgumentException("Unsupported lookup variant " + lookupVariant + " for JMS URI " + soapJmsUri.toString());
        }

        return destination;
    }

    static Destination getReplyToDestination(SoapJmsUri soapJmsUri, Session session) throws NamingException, JMSException {
        Destination destination = null;

        String lookupVariant = soapJmsUri.getLookupVariant();
        if (SoapJmsUri.JNDI_LOOKUP_VARIANT.equals(lookupVariant)) {
            String destinationName = soapJmsUri.getParameter("replyToName");
            if (destinationName != null) {
                destination = (Destination) getContext(soapJmsUri).lookup(destinationName);
            }
        } else if (SoapJmsUri.SEED_QUEUE_LOOKUP_VARIANT.equals(lookupVariant) || SoapJmsUri.SEED_TOPIC_LOOKUP_VARIANT.equals(lookupVariant)) {
            String queueName = soapJmsUri.getParameter("replyToName");
            String topicName = soapJmsUri.getParameter("topicReplyToName");

            if (queueName != null) {
                destination = session.createQueue(queueName);
            } else if (topicName != null) {
                destination = session.createTopic(topicName);
            }
        } else {
            throw new IllegalArgumentException("Unsupported lookup variant " + lookupVariant + " for JMS URI " + soapJmsUri.toString());
        }

        return destination;
    }

    @Override
    public String toString() {
        return uri2string;
    }

    String getParameter(String name) {
        String[] values = params.get(name);

        if (values != null && values.length > 0) {
            return params.get(name)[0];
        }

        return null;
    }

    String getLookupVariant() {
        return lookupVariant;
    }

    String getDestinationName() {
        return destinationName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SoapJmsUri that = (SoapJmsUri) o;

        return uri2string.equals(that.uri2string);
    }

    @Override
    public int hashCode() {
        return uri2string.hashCode();
    }
}
