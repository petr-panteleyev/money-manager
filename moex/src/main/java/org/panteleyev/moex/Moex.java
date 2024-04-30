/*
 Copyright © 2023-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex;

import org.panteleyev.moex.client.MoexClient;
import org.panteleyev.moex.model.MoexEngine;
import org.panteleyev.moex.model.MoexMarket;
import org.panteleyev.moex.model.MoexMarketData;
import org.panteleyev.moex.model.MoexSecurity;
import org.panteleyev.moex.xml.MarketDataParser;
import org.panteleyev.moex.xml.SecurityParser;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.StartElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Moex {
    private record NameValue(
            String name,
            String value
    ) {
    }

    private final MoexClient client = new MoexClient();

    public List<MoexEngine> getEngines() {
        var response = client.getEngines();
        if (response.statusCode() != 200) {
            throw new RuntimeException("Request failed");
        }

        var result = new ArrayList<MoexEngine>();

        try (var inputStream = response.body()) {
            var factory = XMLInputFactory.newInstance();
            var eventReader = factory.createXMLEventReader(inputStream);

            while (eventReader.hasNext()) {
                var event = eventReader.nextEvent();

                if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    var startElement = event.asStartElement();
                    if ("row".equals(startElement.getName().getLocalPart())) {
                        parseEngine(startElement).ifPresent(result::add);
                    }
                }
            }

            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<MoexMarket> getMarkets(MoexEngine engine) {
        var response = client.getMarkets(engine);
        if (response.statusCode() != 200) {
            throw new RuntimeException("Request failed");
        }

        var result = new ArrayList<MoexMarket>();

        try (var inputStream = response.body()) {
            var factory = XMLInputFactory.newInstance();
            var eventReader = factory.createXMLEventReader(inputStream);

            while (eventReader.hasNext()) {
                var event = eventReader.nextEvent();

                if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    var startElement = event.asStartElement();
                    if ("row".equals(startElement.getName().getLocalPart())) {
                        parseMarket(engine, startElement).ifPresent(result::add);
                    }
                }
            }

            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Optional<MoexSecurity> getSecurity(String securityId) {
        var response = client.getSecurity(securityId);
        if (response.statusCode() != 200) {
            throw new RuntimeException("Request failed");
        }

        try (var inputStream = response.body()) {
            var docBuilderFactory = DocumentBuilderFactory.newInstance();
            var docBuilder = docBuilderFactory.newDocumentBuilder();
            var document = docBuilder.parse(inputStream);

            return new SecurityParser().parseSecurity(document);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Optional<MoexMarketData> getMarketData(String securityId, String engine, String market, String board) {
        var response = client.getMarketData(securityId, engine, market, board);
        if (response.statusCode() != 200) {
            throw new RuntimeException("Request failed");
        }

        try (var inputStream = response.body()) {
            var docBuilderFactory = DocumentBuilderFactory.newInstance();
            var docBuilder = docBuilderFactory.newDocumentBuilder();
            var document = docBuilder.parse(inputStream);

            return new MarketDataParser().parse(document);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Optional<MoexEngine> parseEngine(StartElement element) {
        var idAttr = element.getAttributeByName(new QName("id"));
        var nameAttr = element.getAttributeByName(new QName("name"));
        var titleAttr = element.getAttributeByName(new QName("title"));

        if (idAttr == null || nameAttr == null || titleAttr == null) {
            return Optional.empty();
        } else {
            return Optional.of(new MoexEngine(
                    Integer.parseInt(idAttr.getValue()),
                    nameAttr.getValue(),
                    titleAttr.getValue()
            ));
        }
    }

    private Optional<MoexMarket> parseMarket(MoexEngine engine, StartElement element) {
        var idAttr = element.getAttributeByName(new QName("id"));
        var nameAttr = element.getAttributeByName(new QName("NAME"));
        var titleAttr = element.getAttributeByName(new QName("title"));

        if (idAttr == null || nameAttr == null || titleAttr == null) {
            return Optional.empty();
        } else {
            return Optional.of(new MoexMarket(
                    Integer.parseInt(idAttr.getValue()),
                    engine,
                    nameAttr.getValue(),
                    titleAttr.getValue()
            ));
        }
    }
}
