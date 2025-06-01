/*
 Copyright © 2018-2025 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.statements;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.panteleyev.money.desktop.commons.DataCache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.Map.entry;

class SberbankParser implements Parser {
    private final static Logger LOGGER = Logger.getLogger(SberbankParser.class.getName());

    private enum Param {
        TEMPLATE_VALUE,
        TABLE, RECORD, HEADER, DETAIL, CATEGORY,
        NAME, ACTUAL_DATE, DATE_CLASS, DATE_VALUE, SUM, AMOUNT,
        GEO, COUNTRY, CITY, VALUE, EXECUTION_DATE,
        CREDIT_CLASSES
    }

    private final static Map<Param, Object> DEFAULT_CLASSES = new EnumMap<>(Map.ofEntries(
            entry(Param.TABLE, "b-trs"),
            entry(Param.RECORD, "trs_it"),
            entry(Param.HEADER, "trs_head"),
            entry(Param.DETAIL, "trs_detail"),
            entry(Param.NAME, "trs_name"),
            entry(Param.ACTUAL_DATE, "trs_date"),
            entry(Param.DATE_CLASS, "idate"),
            entry(Param.DATE_VALUE, "data-date"),
            entry(Param.SUM, "trs_sum"),
            entry(Param.AMOUNT, "trs_sum-am"),
            entry(Param.GEO, "trs-geo"),
            entry(Param.COUNTRY, "trs_country"),
            entry(Param.CITY, "trs_city"),
            entry(Param.VALUE, "trs_val"),
            entry(Param.CATEGORY, "icat"),
            entry(Param.EXECUTION_DATE, "trs-post"),
            entry(Param.CREDIT_CLASSES, List.of("trs_st-refill"))
    ));

    private final static String TEMPLATE_ATTRIBUTE_NAME = "name";
    private final static String TEMPLATE_ATTRIBUTE_VALUE = "template-details";
    private final static String TEMPLATE_VALUE = "content";

    private enum Format {
        UNKNOWN(null),
        HTML_DEBIT_VERSION_2_1_6("HTML_DEBIT_RUS_REPORT, 07.04.2017, 2.1.6"),
        HTML_DEBIT_VERSION_2_1_7("HTML_DEBIT_RUS_REPORT, 25.01.2018, 2.1.17"),
        HTML_DEBIT_VERSION_2_1_26("HTML_DEBIT_RUS_REPORT, 27.11.2018, 2.1.26"),
        HTML_DEBIT_VERSION_2_1_29("HTML_DEBIT_RUS_REPORT, 10.12.2019, 2.1.29"),
        HTML_CREDIT_VERSION_2_1_6("HTML_CREDIT_RUS_REPORT, 07.04.2017, 2.1.6"),
        HTML_CREDIT_VERSION_2_1_26("HTML_CREDIT_RUS_REPORT, 27.11.2018, 2.1.26"),
        HTML_CREDIT_VERSION_2_1_29("HTML_CREDIT_RUS_REPORT, 10.12.2019, 2.1.29");

        private final String formatString;
        private final Map<Param, Object> formatClasses;

        Format(String formatString) {
            this.formatString = formatString;
            this.formatClasses = DEFAULT_CLASSES;
        }

        String getFormatString() {
            return formatString;
        }

        String getString(Param clazz) {
            return (String) formatClasses.get(clazz);
        }

        @SuppressWarnings("unchecked")
        Collection<String> creditClasses() {
            return (Collection<String>) formatClasses.get(Param.CREDIT_CLASSES);
        }

        static Stream<Format> stream() {
            return Arrays.stream(values());
        }

        static Format detectFormat(String formatString) {
            return stream().filter(f -> Objects.equals(f.getFormatString(), formatString))
                    .findFirst()
                    .orElse(Format.UNKNOWN);
        }
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public StatementType detectType(RawStatementData data) {
        var content = data.getContent();
        if (content.contains("HTML_DEBIT_RUS_REPORT") || content.contains("HTML_CREDIT_RUS_REPORT")
                || content.contains("HTML_DEBIT_RUS_HISTORY")) {
            return StatementType.SBERBANK_HTML;
        } else {
            return StatementType.UNKNOWN;
        }
    }

    @Override
    public Statement parse(RawStatementData data, DataCache cache, StatementType type) {
        try (var inputStream = new ByteArrayInputStream(data.getBytes())) {
            return parseCreditCardHtml(inputStream, cache);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static void checkElement(Element element) {
        Objects.requireNonNull(element, "Malformed statement");
    }

    private static LocalDate parseDate(Element dateElement, Format format) {
        var iDate = dateElement.getElementsByClass(format.getString(Param.DATE_CLASS)).first();
        if (iDate != null) {
            var dateString = iDate.attributes().get(format.getString(Param.DATE_VALUE));
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } else {
            return null;
        }
    }

    static Statement parseCreditCardHtml(InputStream inputStream, DataCache cache) {
        var records = new ArrayList<StatementRecord>();

        try {
            var document = Jsoup.parse(inputStream, StandardCharsets.UTF_8.name(), "");

            // Find template version
            var format = parseTemplateFormat(document);

            // Account number
            var accountNumber = "";
            var accountNumberTag = document.getElementsByClass("b-info b-card-info")
                    .select("div.info_item:eq(1)")
                    .select("div.info_value")
                    .first();
            if (accountNumberTag != null) {
                accountNumber = accountNumberTag.text();
            }

            if (accountNumber.isEmpty()) {
                LOGGER.warning("Account number not found");
            } else {
                accountNumber = accountNumber.replaceAll(" ", "");
            }

            // Transaction table
            var transactionTable = document.getElementsByClass(format.getString(Param.TABLE)).first();
            if (transactionTable == null) {
                LOGGER.warning("Transactions not found in statement");
                return new Statement(StatementType.SBERBANK_HTML, accountNumber, records);
            }

            var transactionList = transactionTable.getElementsByClass(format.getString(Param.RECORD));
            if (transactionList.isEmpty()) {
                LOGGER.warning("Transactions not found in statement");
                return new Statement(StatementType.SBERBANK_HTML, accountNumber, records);
            }

            for (var transaction : transactionList) {
                var head = transaction.getElementsByClass(format.getString(Param.HEADER)).first();
                if (head == null) {
                    continue;
                }

                var builder = new StatementRecord.Builder();

                var nameElement = head.getElementsByClass(format.getString(Param.NAME)).first();
                checkElement(nameElement);
                builder = builder.counterParty(nameElement.text());

                // Transaction actual date
                var dateElement = head.getElementsByClass(format.getString(Param.ACTUAL_DATE)).first();
                checkElement(dateElement);
                var transactionDate = parseDate(dateElement, format);
                builder = builder.actual(transactionDate);

                // Transaction amount
                builder = builder.amount(parseTransactionAmount(head, format));

                // Transaction category
                var catElement = head.getElementsByClass(format.getString(Param.CATEGORY)).first();
                var category = catElement != null ? catElement.text() : "";
                builder = builder.description(category);

                // Additional details
                var detailsList = transaction.getElementsByClass(format.getString(Param.DETAIL));
                for (var detail : detailsList) {
                    var classNames = detail.classNames();

                    if (classNames.contains(format.getString(Param.EXECUTION_DATE))) {
                        builder = builder.execution(parseDate(
                                detail.getElementsByClass(format.getString(Param.VALUE)).first(), format));
                    } else if (classNames.contains(format.getString(Param.GEO))) {
                        var countryElement =
                                detail.getElementsByClass(format.getString(Param.COUNTRY)).first();
                        if (countryElement != null) {
                            builder = builder.country(countryElement.text());
                        }
                        var cityElement = detail.getElementsByClass(format.getString(Param.CITY)).first();
                        if (cityElement != null) {
                            builder = builder.place(cityElement.text());
                        }
                    }
                }

                records.add(builder.build(cache));
            }

            return new Statement(StatementType.SBERBANK_HTML, accountNumber, records);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static Format parseTemplateFormat(Document document) {
        var format = Format.UNKNOWN;
        var version = "";
        var versionElement =
                document.getElementsByAttributeValue(TEMPLATE_ATTRIBUTE_NAME, TEMPLATE_ATTRIBUTE_VALUE).first();
        if (versionElement != null) {
            var attributes = versionElement.attributes();
            version = attributes.get(TEMPLATE_VALUE);
            format = Format.detectFormat(version);
        }

        if (format == Format.UNKNOWN) {
            LOGGER.warning("Sberbank format not recognized: " + version);
        }

        return format;
    }

    private static String parseTransactionAmount(Element head, Format format) {
        var sumElement = head.getElementsByClass(format.getString(Param.SUM)).first();
        checkElement(sumElement);
        var amountElement = sumElement.getElementsByClass(format.getString(Param.AMOUNT)).first();
        checkElement(amountElement);
        var sumString = amountElement.text();

        // Check if transaction is credit to the account
        boolean credit = false;
        for (var crClass : format.creditClasses()) {
            if (!sumElement.getElementsByClass(crClass).isEmpty()) {
                credit = true;
                break;
            }
        }

        if (!credit) {
            sumString = '-' + sumString;
        }

        return sumString;
    }
}
