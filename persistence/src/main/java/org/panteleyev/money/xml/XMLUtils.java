package org.panteleyev.money.xml;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

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
