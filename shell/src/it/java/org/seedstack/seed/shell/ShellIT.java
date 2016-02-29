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
import org.apache.commons.lang.StringUtils;
import org.fusesource.jansi.AnsiString;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.it.AbstractSeedIT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

import static org.assertj.core.api.Assertions.assertThat;

public class ShellIT extends AbstractSeedIT {
    @Configuration("shell.port")
    private int shellPort;

    SSHClient sshClient;
    Session session;

    @Before
    public void connect() throws Exception {
        sshClient = new SSHClient();
        sshClient.addHostKeyVerifier((hostname, port, key) -> true);
        sshClient.connect(InetAddress.getLocalHost(), shellPort);
        sshClient.authPassword("test", "good");
        session = sshClient.startSession();
    }

    @After
    public void disconnect() throws Exception {
        if (session != null) {
            session.close();
        }

        if (sshClient != null) {
            sshClient.disconnect();
        }
    }

    @Test
    public void basic_command_execution_is_working() throws Exception {
        assertThat(execute("test:list")).isEqualTo("[1,2,3,4,5]");
    }

    @Test
    public void command_with_short_option_with_argument_is_working() throws Exception {
        assertThat(execute("test:withoptions -atest")).isEqualTo("with-argument: test");
    }

    @Test
    public void command_with_long_option_with_argument_is_working() throws Exception {
        assertThat(execute("test:withoptions --with-argument=test")).isEqualTo("with-argument: test");
    }

    @Test
    public void command_with_short_option_without_argument_is_working() throws Exception {
        assertThat(execute("test:withoptions -n")).isEqualTo("without-argument");
    }

    @Test
    public void command_with_long_option_without_argument_is_working() throws Exception {
        assertThat(execute("test:withoptions --no-argument")).isEqualTo("without-argument");
    }

    @Test
    public void command_with_allowed_security() throws Exception {
        assertThat(execute("test:allowed")).isEqualTo("allowed");
    }

    @Test
    public void piped_command_execution_is_not_working() throws Exception {
        assertThat(execute("test:list | count")).isNull();
    }

    @Test
    public void command_with_denied_security() throws Exception {
        String actual = execute("test:denied");
        assertThat(actual).isEqualTo(null);
    }

    private String execute(String line) throws IOException {
        Session.Command command = session.exec(line);

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(command.getInputStream()));
            return ansiToString(bufferedReader.readLine());
        } finally {
            command.close();
        }
    }

    private String ansiToString(String actual) {
        if (StringUtils.isNotBlank(actual)) {
            return new AnsiString(actual).getPlain().toString();
        } else {
            return actual;
        }
    }
}
