/*
 Copyright (C) 2021, 2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.app.settings;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;
import static org.panteleyev.money.xml.XMLUtils.appendObjectTextNode;
import static org.panteleyev.money.xml.XMLUtils.createDocument;
import static org.panteleyev.money.xml.XMLUtils.getBooleanNodeValue;
import static org.panteleyev.money.xml.XMLUtils.getIntNodeValue;
import static org.panteleyev.money.xml.XMLUtils.getStringNodeValue;
import static org.panteleyev.money.xml.XMLUtils.readDocument;
import static org.panteleyev.money.xml.XMLUtils.writeDocument;

final class GeneralSettings {
    private static final String ROOT = "settings";

    enum Setting {
        AUTO_COMPLETE_LENGTH("autoCompleteLength", 3),
        ACCOUNT_CLOSING_DAY_DELTA("accountClosingDayDelta", 10),
        SHOW_DEACTIVATED_ACCOUNTS("showDeactivatedAccounts", false),
        LAST_STATEMENT_DIR("lastStatementDir", ""),
        LAST_EXPORT_DIR("lastExportDir", "");

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
        return (T) settings.computeIfAbsent(key, k -> key.getDefaultValue());
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
            // TODO: reimplement with switch pattern matching when available
            Optional<?> value = Optional.empty();
            if (key.getDefaultValue() instanceof Integer) {
                value = getIntNodeValue(rootElement, key.getElementName());
            } else if (key.getDefaultValue() instanceof String) {
                value = getStringNodeValue(rootElement, key.getElementName());
            } else if (key.getDefaultValue() instanceof Boolean) {
                value = getBooleanNodeValue(rootElement, key.getElementName());
            }
            value.ifPresent(x -> settings.put(key, x));
        }
    }
}
