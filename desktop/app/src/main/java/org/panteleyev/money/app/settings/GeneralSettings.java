/*
 Copyright © 2021-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.settings;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;
import static org.panteleyev.money.desktop.commons.xml.XMLUtils.appendObjectTextNode;
import static org.panteleyev.money.desktop.commons.xml.XMLUtils.createDocument;
import static org.panteleyev.money.desktop.commons.xml.XMLUtils.getBooleanNodeValue;
import static org.panteleyev.money.desktop.commons.xml.XMLUtils.getIntNodeValue;
import static org.panteleyev.money.desktop.commons.xml.XMLUtils.getStringNodeValue;
import static org.panteleyev.money.desktop.commons.xml.XMLUtils.readDocument;
import static org.panteleyev.money.desktop.commons.xml.XMLUtils.writeDocument;

final class GeneralSettings {
    private static final String ROOT = "settings";

    enum Setting {
        AUTO_COMPLETE_LENGTH("autoCompleteLength", 3),
        ACCOUNT_CLOSING_DAY_DELTA("accountClosingDayDelta", 10),
        PERIODIC_PAYMENT_DAY_DELTA("periodicPaymentDayDelta", 5),
        SHOW_DEACTIVATED_ACCOUNTS("showDeactivatedAccounts", false),
        SHOW_DEACTIVATED_CARDS("showDeactivatedCards", false),
        LAST_STATEMENT_DIR("lastStatementDir", ""),
        LAST_EXPORT_DIR("lastExportDir", ""),
        LAST_REPORT_DIR("lastReportDir", "");

        private final String elementName;
        private final Object defaultValue;

        Setting(String elementName, Object defaultValue) {
            this.elementName = elementName;
            this.defaultValue = defaultValue;
        }

        public String getElementName() {
            return elementName;
        }

        public Object getDefaultValue() {
            return defaultValue;
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
        var root = createDocument(ROOT);
        for (var key : Setting.values()) {
            appendObjectTextNode(root, key.getElementName(), get(key));
        }
        writeDocument(root.getOwnerDocument(), out);
    }

    void load(InputStream in) {
        var rootElement = readDocument(in);

        for (var key : Setting.values()) {
            var value = switch (key.getDefaultValue()) {
                case Integer _ -> getIntNodeValue(rootElement, key.getElementName());
                case String _ -> getStringNodeValue(rootElement, key.getElementName());
                case Boolean _ -> getBooleanNodeValue(rootElement, key.getElementName());
                default -> Optional.empty();
            };
            value.ifPresent(x -> settings.put(key, x));
        }
    }
}
