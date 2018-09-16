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

import com.jcraft.jsch.JSch;
import org.panteleyev.money.Options;
import org.panteleyev.money.profiles.ConnectionProfile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static org.panteleyev.money.XMLUtils.appendElement;
import static org.panteleyev.money.XMLUtils.appendTextNode;
import static org.panteleyev.money.XMLUtils.createDocument;
import static org.panteleyev.money.XMLUtils.writeDocument;

public class SshManager {
    private static final String SSH_CONFIG = "ssh.xml";

    private static JSch jSch = null;

    private static final Map<String, SshSession> sessions = new HashMap<>();

    static JSch getjSch() {
        synchronized (SshManager.class) {
            if (jSch == null) {
                jSch = new JSch();
            }

            return jSch;
        }
    }

    public static void closeAllSessions() {
        for (var session : sessions.values()) {
            session.disconnect();
        }
    }

    static void saveSessions(OutputStream out) {
        var rootElement = createDocument("MoneyManager");
        var doc = rootElement.getOwnerDocument();

        var root = appendElement(rootElement, "sshSessions");
        for (var session : sessions.values()) {
            root.appendChild(exportSshSession(doc, session));
        }

        writeDocument(doc, out);
    }

    static void saveSessions() {
        var file = new File(Options.getSettingsDirectory(), SSH_CONFIG);
        try (var out = new FileOutputStream(file)) {
            saveSessions(out);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

    }

    static void loadSessions(InputStream inputStream) throws Exception {
        var factory = SAXParserFactory.newInstance();
        var parser = factory.newSAXParser();

        var importParser = new SshXmlParser();
        parser.parse(inputStream, importParser);

        importParser.getSessions().forEach(p -> sessions.put(p.getName(), p));
    }

    public static void loadSessions() {
        sessions.clear();

        var file = new File(Options.getSettingsDirectory(), SSH_CONFIG);
        if (!file.exists()) {
            return;
        }

        try (var inputStream = new FileInputStream(file)) {
            loadSessions(inputStream);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static void setSessions(Collection<SshSession> sessionList) {
        sessions.clear();
        sessionList.forEach(s -> sessions.put(s.getName(), s));
    }

    public static Collection<SshSession> getSessions() {
        return sessions.values();
    }

    private static Optional<SshSession> getSession(String name) {
        return name == null || name.isEmpty() ?
                Optional.empty() : Optional.ofNullable(sessions.get(name));
    }

    public static void setupTunnel(ConnectionProfile profile) {
        getSession(profile.getSshSession()).ifPresent(SshSession::connect);
    }

    private static Element exportSshSession(Document doc, SshSession session) {
        var e = doc.createElement("session");

        appendTextNode(e, "name", session.getName());
        appendTextNode(e, "host", session.getHost());
        appendTextNode(e, "port", session.getPort());
        appendTextNode(e, "user", session.getUser());
        appendTextNode(e, "privateKeyPath", session.getPrivateKeyPath());
        appendTextNode(e, "localPort", session.getLocalPort());

        return e;
    }
}
