/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.math.BigDecimal;
import java.util.Optional;

import static org.panteleyev.moex.xml.ParserUtil.parseInt;
import static org.panteleyev.moex.xml.ParserUtil.parseNumber;

public class BaseParser {
    private final XPath xPath;

    public BaseParser() {
        var xPathFactory = XPathFactory.newInstance();
        xPath = xPathFactory.newXPath();
    }

    public XPath xPath() {
        return xPath;
    }

    String getString(Object node, String expr) throws Exception {
        return (String) xPath.compile(expr).evaluate(node, XPathConstants.STRING);
    }

    BigDecimal getNumber(Object node, String expr) throws Exception {
        var str = getString(node, expr);
        return parseNumber(str);
    }

    Integer getInteger(Object node, String expr) throws  Exception {
        var str = getString(node, expr);
        return parseInt(str);
    }
}
