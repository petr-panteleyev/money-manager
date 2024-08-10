/*
 Copyright © 2021-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.settings;

import org.panteleyev.commons.xml.XMLEventReaderWrapper;
import org.panteleyev.commons.xml.XMLStreamWriterWrapper;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

final class GeneralSettings {
    private static final QName ROOT = new QName("settings");

    enum Setting {
        AUTO_COMPLETE_LENGTH("autoCompleteLength", 3),
        ACCOUNT_CLOSING_DAY_DELTA("accountClosingDayDelta", 10),
        PERIODIC_PAYMENT_DAY_DELTA("periodicPaymentDayDelta", 5),
        SHOW_DEACTIVATED_ACCOUNTS("showDeactivatedAccounts", false),
        SHOW_DEACTIVATED_CARDS("showDeactivatedCards", false),
        LAST_STATEMENT_DIR("lastStatementDir", ""),
        LAST_EXPORT_DIR("lastExportDir", ""),
        LAST_REPORT_DIR("lastReportDir", "");

        private final QName elementName;
        private final Object defaultValue;

        Setting(String elementName, Object defaultValue) {
            this.elementName = new QName(elementName);
            this.defaultValue = defaultValue;
        }

        public QName getElementName() {
            return elementName;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        static Optional<Setting> of(QName name) {
            return Arrays.stream(values())
                    .filter(v -> v.getElementName().equals(name))
                    .findAny();
        }
    }

    private final Map<Setting, Object> settings = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    <T> T get(Setting key) {
        return (T) settings.computeIfAbsent(key, _ -> key.getDefaultValue());
    }

    void put(Setting key, Object value) {
        settings.put(key, requireNonNull(value));
    }

    void save(OutputStream out) {
        try (var wrapper = XMLStreamWriterWrapper.newInstance(out)) {
            wrapper.document(ROOT, () -> {
                for (var key : Setting.values()) {
                    wrapper.textElement(key.getElementName(), get(key));
                }
            });
        }
    }

    void load(InputStream in) {
        try (var reader = XMLEventReaderWrapper.newInstance(in)) {
            while (reader.hasNext()) {
                var event = reader.nextEvent();
                event.asStartElement().flatMap(element -> Setting.of(element.getName()))
                        .ifPresent(setting -> reader.getElementText().flatMap(text -> parseValue(setting, text))
                                .ifPresent(value -> put(setting, value)));
            }
        }
    }

    private static Optional<Object> parseValue(Setting setting, String text) {
        try {
            return switch (setting.getDefaultValue()) {
                case Integer _ -> Optional.of(Integer.parseInt(text));
                case Boolean _ -> Optional.of(Boolean.parseBoolean(text));
                case String _ -> Optional.of(text);
                default -> Optional.empty();
            };
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
