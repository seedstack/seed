/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.configuration;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.function.Predicate;
import javax.net.ServerSocketFactory;
import org.seedstack.coffig.spi.ConfigFunction;
import org.seedstack.coffig.spi.ConfigFunctionHolder;

public class AvailablePortFunctionHolder implements ConfigFunctionHolder {
    private static final int PORT_RANGE_MIN = 49152;
    private static final int PORT_RANGE_MAX = 65535;

    @ConfigFunction
    int availableTcpPort() {
        return findPort(PORT_RANGE_MIN, PORT_RANGE_MAX, this::isTcpPortAvailable);
    }

    @ConfigFunction
    int availableUdpPort() {
        return findPort(PORT_RANGE_MIN, PORT_RANGE_MAX, this::isUdpPortAvailable);
    }

    private int findPort(int min, int max, Predicate<Integer> availablePredicate) {
        for (int i = min; i <= max; i++) {
            if (availablePredicate.test(i)) {
                return i;
            }
        }
        throw new IllegalStateException("Unable to find an available port in range " + min + "-" + max);
    }

    private boolean isTcpPortAvailable(int port) {
        try (ServerSocket socket = ServerSocketFactory.getDefault().createServerSocket(
                port, 1, InetAddress.getByName("localhost"))) {
            socket.setReuseAddress(true);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isUdpPortAvailable(int port) {
        try (DatagramSocket socket = new DatagramSocket(port, InetAddress.getByName("localhost"))) {
            socket.setReuseAddress(true);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
