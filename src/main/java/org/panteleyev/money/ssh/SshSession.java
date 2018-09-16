/*
 * Copyright (c) 2018, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money.ssh;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.util.Objects;
import java.util.Properties;

public final class SshSession {
    private static final String LHOST = "localhost";
    private static final int SSH_PORT = 22;

    private static final int KEEP_ALIVE_INTERVAL_MS = 1000;
    private static final int KEEP_ALIVE_COUNT_MAX = 10;

    private final String name;
    private final String host;
    private final int port;
    private final String user;
    private final String privateKeyPath;
    private final int localPort;

    private Session session = null;

    SshSession(String name, String host, int port, String user, String privateKeyPath, int localPort) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.user = user;
        this.privateKeyPath = privateKeyPath;
        this.localPort = localPort;
    }

    public String getName() {
        return name;
    }

    String getHost() {
        return host;
    }

    int getPort() {
        return port;
    }

    String getUser() {
        return user;
    }

    String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public int getLocalPort() {
        return localPort;
    }

    void connect() {
        if (session != null) {
            return;
        }

        try {
            var jSch = SshManager.getjSch();
            if (privateKeyPath != null) {
                jSch.addIdentity(privateKeyPath);
            }

            var config = new Properties();
            config.put("StrictHostKeyChecking", "no");

            session = jSch.getSession(user, host, SSH_PORT);
            session.setConfig(config);
            session.connect();
            session.setServerAliveInterval(KEEP_ALIVE_INTERVAL_MS);
            session.setServerAliveCountMax(KEEP_ALIVE_COUNT_MAX);

            session.setPortForwardingL(localPort, LHOST, port);
        } catch (JSchException ex) {
            throw new RuntimeException(ex);
        }
    }

    void disconnect() {
        if (session == null || !session.isConnected()) {
            return;
        }

        session.disconnect();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof SshSession)) {
            return false;
        }

        var that = (SshSession) object;

        return Objects.equals(this.name, that.name)
                && Objects.equals(this.host, that.host)
                && this.port == that.port
                && Objects.equals(this.user, that.user)
                && Objects.equals(this.privateKeyPath, that.privateKeyPath)
                && this.localPort == that.localPort;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, host, port, user, privateKeyPath, localPort);
    }
}
