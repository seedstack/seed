/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import org.seedstack.coffig.spi.ConfigFunction;
import org.seedstack.coffig.spi.ConfigFunctionHolder;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AvailablePortFunctionHolder implements ConfigFunctionHolder {
    private static final ConcurrentMap<String, Integer> TCP_PORTS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Integer> UDP_PORTS = new ConcurrentHashMap<>();
    private static final int PORT_RANGE_MIN = 49152;
    private static final int PORT_RANGE_MAX = 65535;

    @ConfigFunction
    int availableTcpPort(String name) {
        return TCP_PORTS.computeIfAbsent(name, n -> {
            synchronized (TCP_PORTS) {
                for (int i = PORT_RANGE_MIN; i <= PORT_RANGE_MAX; i++) {
                    if (isTcpPortAvailable(i)) {
                        return i;
                    }
                }
                throw new IllegalStateException("Unable to find an available TCP port in range " + PORT_RANGE_MIN + "-" + PORT_RANGE_MAX);
            }
        });
    }

    @ConfigFunction
    int availableUdpPort(String name) {
        return UDP_PORTS.computeIfAbsent(name, n -> {
            synchronized (UDP_PORTS) {
                for (int i = PORT_RANGE_MIN; i <= PORT_RANGE_MAX; i++) {
                    if (isUdpPortAvailable(i)) {
                        return i;
                    }
                }
            }
            throw new IllegalStateException("Unable to find an available UDP port in range " + PORT_RANGE_MIN + "-" + PORT_RANGE_MAX);
        });
    }

    private boolean isTcpPortAvailable(int port) {
        try (ServerSocket socket = ServerSocketFactory.getDefault().createServerSocket(
                port, 1, InetAddress.getByName("localhost"))) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    private boolean isUdpPortAvailable(int port) {
        try (DatagramSocket socket = new DatagramSocket(port, InetAddress.getByName("localhost"))) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }
}
