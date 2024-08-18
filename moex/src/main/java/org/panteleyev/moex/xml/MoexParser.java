/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex.xml;

import org.panteleyev.commons.xml.StartElementWrapper;
import org.panteleyev.commons.xml.XMLEventReaderWrapper;
import org.panteleyev.moex.model.MoexEngine;
import org.panteleyev.moex.model.MoexMarket;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoexParser {
    private static final QName ROW = new QName("row");
    private static final QName ATTR_ID = new QName("id");
    private static final QName ATTR_NAME = new QName("name");
    private static final QName ATTR_NAME_UPPER = new QName("NAME");
    private static final QName ATTR_TITLE = new QName("title");

    public List<MoexEngine> getEngines(InputStream inputStream) {
        try (var eventReader = XMLEventReaderWrapper.newInstance(inputStream)) {
            var result = new ArrayList<MoexEngine>();

            while (eventReader.hasNext()) {
                eventReader.nextEvent()
                        .asStartElement(ROW)
                        .flatMap(this::parseEngine)
                        .ifPresent(result::add);
            }

            return result;
        }
    }

    private Optional<MoexEngine> parseEngine(StartElementWrapper element) {
        var idAttr = element.getAttributeValue(ATTR_ID, Integer.class);
        var nameAttr = element.getAttributeValue(ATTR_NAME);
        var titleAttr = element.getAttributeValue(ATTR_TITLE);

        if (idAttr.isEmpty() || nameAttr.isEmpty() || titleAttr.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new MoexEngine(
                    idAttr.get(),
                    nameAttr.get(),
                    titleAttr.get()
            ));
        }
    }

    public List<MoexMarket> getMarkets(InputStream inputStream, MoexEngine engine) {
        try (var eventReader = XMLEventReaderWrapper.newInstance(inputStream)) {
            var result = new ArrayList<MoexMarket>();

            while (eventReader.hasNext()) {
                eventReader.nextEvent()
                        .asStartElement(ROW)
                        .flatMap(element -> parseMarket(engine, element))
                        .ifPresent(result::add);
            }

            return result;
        }
    }

    private Optional<MoexMarket> parseMarket(MoexEngine engine, StartElementWrapper element) {
        var idAttr = element.getAttributeValue(ATTR_ID, Integer.class);
        var nameAttr = element.getAttributeValue(ATTR_NAME_UPPER);
        var titleAttr = element.getAttributeValue(ATTR_TITLE);

        if (idAttr.isEmpty() || nameAttr.isEmpty() || titleAttr.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new MoexMarket(
                    idAttr.get(),
                    engine,
                    nameAttr.get(),
                    titleAttr.get()
            ));
        }
    }
}
