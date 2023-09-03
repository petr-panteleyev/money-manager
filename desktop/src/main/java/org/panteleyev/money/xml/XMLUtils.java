/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
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
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public final class XMLUtils {
    public static void appendTextNode(Element e, String name, String value) {
        if (value == null) {
            return;
        }

        var document = e.getOwnerDocument();
        var child = document.createElement(name);
        e.appendChild(child);
        var text = document.createTextNode(value);
        child.appendChild(text);
    }

    public static void appendTextNode(Element e, String name, Integer value) {
        if (value != null) {
            appendTextNode(e, name, Integer.toString(value));
        }
    }

    public static void appendTextNode(Element e, String name, boolean value) {
        appendTextNode(e, name, Boolean.toString(value));
    }

    public static void appendTextNode(Element e, String name, long value) {
        appendTextNode(e, name, Long.toString(value));
    }

    public static void appendTextNode(Element e, String name, BigDecimal value) {
        if (value != null) {
            appendTextNode(e, name, value.toString());
        }
    }

    public static void appendTextNode(Element e, String name, LocalDate value) {
        if (value != null) {
            appendTextNode(e, name, value.toEpochDay());
        }
    }

    public static void appendTextNode(Element e, String name, UUID value) {
        if (value != null) {
            appendTextNode(e, name, value.toString());
        }
    }

    public static void appendTextNode(Element e, String name, byte[] value) {
        if (value != null) {
            appendTextNode(e, name, Base64.getEncoder().encodeToString(value));
        }
    }

    public static void appendTextNode(Element e, String name, Enum<?> value) {
        if (value != null) {
            appendTextNode(e, name, value.name());
        }
    }

    public static void appendObjectTextNode(Element e, String name, Object value) {
        switch (value) {
            case Integer intValue -> appendTextNode(e, name, intValue);
            case String stringValue -> appendTextNode(e, name, stringValue);
            case Boolean booleanValue -> appendTextNode(e, name, booleanValue);
            default -> throw new IllegalArgumentException("Unsupported value type");
        }
    }

    public static Element appendElement(Element parent, String name) {
        var element = parent.getOwnerDocument().createElement(name);
        parent.appendChild(element);
        return element;
    }

    public static Element createDocument(String rootElementName) {
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

    public static Element readDocument(InputStream in) {
        try {
            var docFactory = DocumentBuilderFactory.newInstance();
            var docBuilder = docFactory.newDocumentBuilder();
            var doc = docBuilder.parse(in);
            return doc.getDocumentElement();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void writeDocument(Document document, OutputStream outputStream) {
        try {
            var transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(new DOMSource(document), new StreamResult(outputStream));
        } catch (TransformerException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String getAttribute(Element element, String name, String defValue) {
        var value = element.getAttribute(name);
        return value.isEmpty() ? defValue : value;
    }

    public static int getAttribute(Element element, String name, int defValue) {
        var value = element.getAttribute(name);
        return value.isBlank() ? defValue : Integer.parseInt(value);
    }

    public static double getAttribute(Element element, String name, double defValue) {
        var value = element.getAttribute(name);
        return value.isBlank() ? defValue : Double.parseDouble(value);
    }

    public static boolean getAttribute(Element element, String name, boolean defValue) {
        var value = element.getAttribute(name);
        return value.isBlank() ? defValue : Boolean.parseBoolean(value);
    }

    public static Optional<String> getStringNodeValue(Element parent, String tagName) {
        var nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            var node = nodes.item(i);
            if (node instanceof Element element && element.getTagName().equals(tagName)) {
                return Optional.of(element.getTextContent());
            }
        }
        return Optional.empty();
    }

    public static Optional<Integer> getIntNodeValue(Element parent, String tagName) {
        return getStringNodeValue(parent, tagName)
                .map(Integer::parseInt);
    }

    public static Optional<Boolean> getBooleanNodeValue(Element parent, String tagName) {
        return getStringNodeValue(parent, tagName)
                .map(Boolean::parseBoolean);
    }

    private XMLUtils() {
    }
}
