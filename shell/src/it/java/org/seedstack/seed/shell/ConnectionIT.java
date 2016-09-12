/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.userauth.UserAuthException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.it.AbstractSeedIT;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ConnectionIT extends AbstractSeedIT {

    @Configuration("shell.port")
    private int shellPort;

    SSHClient sshClient;

    @Before
    public void connect() throws Exception {
        sshClient = new SSHClient();
        sshClient.addHostKeyVerifier((hostname, port, key) -> true);
        sshClient.connect(InetAddress.getLocalHost(), shellPort);
    }

    @After
    public void disconnect() throws Exception {
        sshClient.disconnect();
    }

    @Test
    public void ssh_server_is_responding() throws Exception {
        assertThat(sshClient.isConnected()).isTrue();
    }

    @Test
    public void valid_credentials_are_accepted() throws Exception {
        sshClient.authPassword("test", "good");
        assertThat(sshClient.isAuthenticated()).isTrue();
    }

    @Test(expected = UserAuthException.class)
    public void bad_credentials_are_rejected() throws Exception {
        sshClient.authPassword("test", "bad");
        fail("bad credentials should be rejected");
    }

    @Test
    public void welcome_message_and_prompt_are_printed_in_interactive_mode() throws Exception {
        sshClient.authPassword("test", "good");

        Session session = sshClient.startSession();
        Session.Shell shell = session.startShell();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(shell.getInputStream()));
        assertThat(bufferedReader.readLine()).isEqualTo("\u001B[2J\u001B[1;1H\u001B[92m    _______________    ______ ________   __ ");
        assertThat(bufferedReader.readLine()).isEqualTo("   / __/ __/ __/ _ \\  / __/ // / __/ /  / / ");
        bufferedReader.readLine();
        bufferedReader.readLine();
        bufferedReader.readLine();
        bufferedReader.readLine();
        char[] buffer = new char[2048];
        bufferedReader.read(buffer);

        assertThat(new String(buffer).trim()).isEqualTo("test@testapp$");

        shell.close();
        session.close();
    }

    @Test
    public void nothing_is_printed_in_direct_mode() throws Exception {
        sshClient.authPassword("test", "good");

        Session session = sshClient.startSession();

        assertThat(session.getInputStream().available()).isEqualTo(0);

        session.close();
    }

}
