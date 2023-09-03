/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex.xml;

import org.panteleyev.moex.model.MoexSecurity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPathConstants;
import java.util.Objects;
import java.util.Optional;

public class SecurityParser extends BaseParser {
    private static final String DESCRIPTION_XPATH = "/document/data[@id='description']";

    private static final String SECID = "rows/row[@name='SECID']/@value";
    private static final String NAME = "rows/row[@name='NAME']/@value";
    private static final String SHORTNAME = "rows/row[@name='SHORTNAME']/@value";
    private static final String ISIN = "rows/row[@name='ISIN']/@value";
    private static final String REGNUMBER = "rows/row[@name='REGNUMBER']/@value";
    private static final String FACEVALUE = "rows/row[@name='FACEVALUE']/@value";
    private static final String ISSUEDATE = "rows/row[@name='ISSUEDATE']/@value";
    private static final String TYPENAME = "rows/row[@name='TYPENAME']/@value";
    private static final String TYPE = "rows/row[@name='TYPE']/@value";
    private static final String GROUP = "rows/row[@name='GROUP']/@value";
    private static final String GROUPNAME = "rows/row[@name='GROUPNAME']/@value";
    private static final String MATDATE = "rows/row[@name='MATDATE']/@value";
    private static final String DAYSTOREDEMPTION = "rows/row[@name='DAYSTOREDEMPTION']/@value";
    private static final String COUPONVALUE = "rows/row[@name='COUPONVALUE']/@value";
    private static final String COUPONPERCENT = "rows/row[@name='COUPONPERCENT']/@value";
    private static final String COUPONDATE = "rows/row[@name='COUPONDATE']/@value";
    private static final String COUPONFREQUENCY = "rows/row[@name='COUPONFREQUENCY']/@value";

    private static final String PRIMARY_BOARD_XPATH = "/document/data[@id='boards']/rows/row[@is_primary='1']";

    public SecurityParser() {
    }

    public Optional<MoexSecurity> parseSecurity(Document document) throws Exception {
        var builder = new MoexSecurity.Builder();

        var description = xPath().compile(DESCRIPTION_XPATH).evaluate(document, XPathConstants.NODE);

        var secId = getString(description, SECID);
        if (secId.isEmpty()) {
            return Optional.empty();
        }

        builder.secId(getString(description, SECID))
                .name(getString(description, NAME))
                .shortName(getString(description, SHORTNAME))
                .isin(getString(description, ISIN))
                .regNumber(getString(description, REGNUMBER))
                .faceValue(getString(description, FACEVALUE))
                .issueDate(getString(description, ISSUEDATE))
                .type(getString(description, TYPE))
                .typeName(getString(description, TYPENAME))
                .group(getString(description, GROUP))
                .groupName(getString(description, GROUPNAME))
                // Bond specific
                .matDate(getString(description, MATDATE))
                .daysToRedemption(getString(description, DAYSTOREDEMPTION))
                .couponValue(getString(description, COUPONVALUE))
                .couponPercent(getString(description, COUPONPERCENT))
                .couponDate(getString(description, COUPONDATE))
                .couponFrequency(getString(description, COUPONFREQUENCY));

        var primaryBoard = (Element) xPath().compile(PRIMARY_BOARD_XPATH).evaluate(document, XPathConstants.NODE);
        builder.engine(primaryBoard.getAttribute("engine"))
                .market(primaryBoard.getAttribute("market"))
                .primaryBoard(primaryBoard.getAttribute("boardid"));

        return Optional.of(builder.build());
    }
}
