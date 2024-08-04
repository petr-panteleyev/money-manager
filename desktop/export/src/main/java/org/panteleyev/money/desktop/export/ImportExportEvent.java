/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.export;

public record ImportExportEvent(ImportExportEventType type, int level) {
    private static final String INDENT = "    ";

    public ImportExportEvent(ImportExportEventType type) {
        this(type, 0);
    }

    public enum ImportExportEventType {
        DONE("выполнено", true),

        ICONS("значки"),
        CATEGORIES("категории"),
        CURRENCIES("валюты"),
        EXCHANGE_SECURITIES("ценные бумаги"),
        ACCOUNTS("счета"),
        CARDS("карты"),
        CONTACTS("контакты"),
        TRANSACTIONS("проводки"),
        DOCUMENTS("документы"),
        PERIODIC_PAYMENTS("периодические платежи"),
        INVESTMENTS_DEALS("инвестиционные сделки"),
        EXCHANGE_SECURITY_SPLITS("сплиты ценных бумаг"),
        BLOBS("файлы");

        private final String text;
        private final boolean endOfEvent;

        ImportExportEventType(String text, boolean endOfEvent) {
            this.text = text;
            this.endOfEvent = endOfEvent;
        }

        ImportExportEventType(String text) {
            this(text, false);
        }

        public String getText() {
            return text;
        }

        public boolean isEndOfEvent() {
            return endOfEvent;
        }
    }

    public String buildEventString() {
        var builder = new StringBuilder(INDENT.repeat(Math.max(0, level)));
        builder.append(type.getText());

        if (!type.isEndOfEvent()) {
            builder.append("... ");
        } else {
            builder.append("\n");
        }
        return builder.toString();
    }
}