/*
 Copyright © 2023 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex.xml;

import org.panteleyev.commons.xml.StartElementWrapper;
import org.panteleyev.commons.xml.XMLEventReaderWrapper;
import org.panteleyev.moex.model.MoexSecurity;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public class SecurityParser {
    private static final QName DATA = new QName("data");
    private static final QName ROW = new QName("row");

    private static final QName ATTR_ID = new QName("id");
    private static final QName ATTR_NAME = new QName("name");
    private static final QName ATTR_VALUE = new QName("value");
    private static final QName ATTR_IS_PRIMARY = new QName("is_primary");
    private static final QName ATTR_ENGINE = new QName("engine");
    private static final QName ATTR_MARKET = new QName("market");
    private static final QName ATTR_BOARD_ID = new QName("boardid");

    public Optional<MoexSecurity> parseSecurity(InputStream inputStream) {
        try (var reader = XMLEventReaderWrapper.newInstance(inputStream)) {
            var builder = new MoexSecurity.Builder();

            while (reader.hasNext()) {
                var event = reader.nextEvent();

                event.ifStartElement(DATA, e -> Objects.equals(e.getAttributeValue(ATTR_ID, ""), "description"), _ -> {
                    while (reader.hasNext()) {
                        var descriptionEvent = reader.nextEvent();
                        if (descriptionEvent.isEndElement(DATA)) {
                            break;
                        }

                        descriptionEvent.ifStartElement(ROW, rowElement -> {
                            rowElement.getAttributeValue(ATTR_NAME).ifPresent(name -> {
                                switch (name) {
                                    case "SECID" -> builder.secId(getString(rowElement));
                                    case "NAME" -> builder.name(getString(rowElement));
                                    case "SHORTNAME" -> builder.shortName(getString(rowElement));
                                    case "ISIN" -> builder.isin(getString(rowElement));
                                    case "REGNUMBER" -> builder.regNumber(getString(rowElement));
                                    case "FACEVALUE" ->
                                            builder.faceValue(rowElement.getAttributeValue(ATTR_VALUE, BigDecimal.ZERO));
                                    case "ISSUEDATE" -> builder.issueDate(getLocalDate(rowElement));
                                    case "TYPE" -> builder.type(getString(rowElement));
                                    case "TYPENAME" -> builder.typeName(getString(rowElement));
                                    case "GROUP" -> builder.group(getString(rowElement));
                                    case "GROUPNAME" -> builder.groupName(getString(rowElement));
                                    // Bond specific
                                    case "MATDATE" -> builder.matDate(getLocalDate(rowElement));
                                    case "DAYSTOREDEMPTION" -> builder.daysToRedemption(getInteger(rowElement));
                                    case "COUPONVALUE" -> builder.couponValue(getBigDecimal(rowElement));
                                    case "COUPONPERCENT" -> builder.couponPercent(getBigDecimal(rowElement));
                                    case "COUPONDATE" -> builder.couponDate(getLocalDate(rowElement));
                                    case "COUPONFREQUENCY" -> builder.couponFrequency(getInteger(rowElement));
                                }
                            });
                        });
                    }
                });

                event.ifStartElement(DATA, e -> Objects.equals(e.getAttributeValue(ATTR_ID, ""), "boards"), _ -> {
                    while (reader.hasNext()) {
                        var boardsEvent = reader.nextEvent();
                        if (boardsEvent.isEndElement(DATA)) {
                            break;
                        }

                        boardsEvent.ifStartElement(ROW, boardRow -> {
                            return boardRow.getAttributeValue(ATTR_IS_PRIMARY, "0").equals("1");
                        }, primaryBoard -> {
                            builder.engine(primaryBoard.getAttributeValue(ATTR_ENGINE, ""))
                                    .market(primaryBoard.getAttributeValue(ATTR_MARKET, ""))
                                    .primaryBoard(primaryBoard.getAttributeValue(ATTR_BOARD_ID, ""));
                        });

                    }
                });
            }

            return builder.secId().isBlank() ? Optional.empty() : Optional.of(builder.build());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private String getString(StartElementWrapper element) {
        return element.getAttributeValue(ATTR_VALUE, "");
    }

    private LocalDate getLocalDate(StartElementWrapper element) {
        return element.getAttributeValue(ATTR_VALUE, LocalDate.class).orElse(null);
    }

    private Integer getInteger(StartElementWrapper element) {
        return element.getAttributeValue(ATTR_VALUE, Integer.class).orElse(null);
    }

    private BigDecimal getBigDecimal(StartElementWrapper element) {
        return element.getAttributeValue(ATTR_VALUE, BigDecimal.class).orElse(null);
    }
}
