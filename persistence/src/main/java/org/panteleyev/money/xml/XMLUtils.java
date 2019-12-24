/*
 * Copyright (c) 2019, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

public interface XMLUtils {
    static void appendTextNode(Element e, String name, String value) {
        var document = e.getOwnerDocument();
        var child = document.createElement(name);
        e.appendChild(child);
        var text = document.createTextNode(value);
        child.appendChild(text);
    }

    static void appendTextNode(Element e, String name, int value) {
        appendTextNode(e, name, Integer.toString(value));
    }

    static void appendTextNode(Element e, String name, boolean value) {
        appendTextNode(e, name, Boolean.toString(value));
    }

    static void appendTextNode(Element e, String name, long value) {
        appendTextNode(e, name, Long.toString(value));
    }

    static void appendTextNode(Element e, String name, BigDecimal value) {
        appendTextNode(e, name, value.toString());
    }

    static void appendTextNode(Element e, String name, LocalDate value) {
        appendTextNode(e, name, value.toEpochDay());
    }

    static void appendTextNode(Element e, String name, UUID value) {
        if (value != null) {
            appendTextNode(e, name, value.toString());
        }
    }

    static void appendTextNode(Element e, String name, byte[] value) {
        appendTextNode(e, name, Base64.getEncoder().encodeToString(value));
    }

    static void appendTextNode(Element e, String name, Enum<?> value) {
        appendTextNode(e, name, value.name());
    }

    static Element appendElement(Element parent, String name) {
        var element = parent.getOwnerDocument().createElement(name);
        parent.appendChild(element);
        return element;
    }

    static Element createDocument(String rootElementName) {
        try {
            var docFactory = DocumentBuilderFactory.newInstance();
            var docBuilder = docFactory.newDocumentBuilder();

            var doc = docBuilder.newDocument();
            var rootElement = doc.createElement(rootElementName);
            doc.appendChild(rootElement);

            return rootElement;
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    static void writeDocument(Document document, OutputStream outputStream) {
        try {
            var transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(document), new StreamResult(outputStream));
        } catch (TransformerException ex) {
            throw new RuntimeException(ex);
        }
    }
}
